package net.lateinit.aicamera.ui.screen

import android.graphics.*
import android.util.*
import androidx.camera.core.*
import androidx.camera.lifecycle.*
import androidx.camera.view.*
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
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

    // 이미지 오버레이를 그릴 Canvas의 크기를 알기 위함
    var canvasWidth by remember { mutableIntStateOf(1) }
    var canvasHeight by remember { mutableIntStateOf(1) }

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
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xAA000000)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // 추론 시간 표시
                    Text(
                        text = "${inferenceTime}ms Inference",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // 탐지된 객체 리스트
                    if (detections.isEmpty()) {
                        Text(
                            text = "객체를 탐지 중...",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            detections.forEach { detection ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = detection.label.uppercase(),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${"%.1f".format(detection.score * 100)}%",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
