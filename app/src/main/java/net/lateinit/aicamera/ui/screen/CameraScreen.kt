package net.lateinit.aicamera.ui.screen

import android.graphics.*
import android.util.*
import androidx.camera.core.*
import androidx.camera.lifecycle.*
import androidx.camera.view.*
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import net.lateinit.aicamera.domain.model.*
import net.lateinit.aicamera.utils.*
import java.util.concurrent.*

@Composable
fun CameraScreen(
    onImageAnalyzed: (Bitmap, Int) -> Unit,
    detections: List<DetectionResult>,
    inferenceTime: Long,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var previewView: PreviewView by remember { mutableStateOf(PreviewView(context)) }

    LaunchedEffect(Unit) {
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(320, 320))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // 최신 프레임만 사용
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, BitmapAnalyzer(context) { bitmap, rotation ->
                    onImageAnalyzed(bitmap, rotation)
                })
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalysis
            )
        } catch (exc: Exception) {
            Log.e("CameraScreen", "Use case binding failed", exc)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 카메라 미리보기
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

        // AI 탐지 결과를 그릴 오버레이 Canvas
        DetectionOverlay(
            detections = detections,
            previewView = previewView,
        )

        AnimatedVisibility(
            visible = detections.isNotEmpty() || inferenceTime > 0,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            InfoCard(detections = detections, inferenceTime = inferenceTime)
        }
    }
}
