package net.lateinit.aicamera.utils

import android.annotation.*
import android.content.*
import android.graphics.*
import android.media.*
import androidx.camera.core.*

/**
 * CameraX의 ImageProxy를 Bitmap으로 변환하고 회전 정보를 제공하는 Analzyer
 */
@SuppressLint("UnsafeOptInUsageError")
class BitmapAnalyzer(
    private val context: Context,
    private val listener: (bitmap: Bitmap, rotation: Int) -> Unit
) : ImageAnalysis.Analyzer {

    // 마지막으로 처리한 프레임 시간 (초당 프레임 수 제한)
    private var lastAnalyzedTimestamp = 0L
    private val frameIntervalMs = 100 // 1초에 최대 10프레임 처리 (조정 가능)

    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalyzedTimestamp < frameIntervalMs) {
            imageProxy.close() // 너무 빠르게 들어오는 프레임은 버림
            return
        }
        lastAnalyzedTimestamp = currentTime

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = imageProxy.image

        if (image != null) {
            val bitmap = image.toBitmap() // Image를 Bitmap으로 변환하는 함수 호출
            listener(bitmap, rotationDegrees)
        }
        imageProxy.close() // 사용한 ImageProxy는 반드시 닫아줘야 메모리 누수를 방지
    }

    // Image를 Bitmap으로 변환하는 헬퍼 함수
    private fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            this.width,
            this.height,
            null
        )
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            Rect(0, 0, yuvImage.width, yuvImage.height),
            90,
            out
        )
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
