package net.lateinit.aicamera.ui.screen

import android.annotation.*
import android.graphics.*
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader.TileMode
import androidx.camera.view.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import net.lateinit.aicamera.domain.model.*
import android.graphics.Color as AndroidColor

@SuppressLint("LocalContextResourcesRead")
@Composable
fun DetectionOverlay(
    detections: List<DetectionResult>,
    previewView: PreviewView,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val scaleX = size.width / previewView.width.toFloat()
        val scaleY = size.height / previewView.height.toFloat()

        // Canvas를 클리어 (이전 프레임의 박스 지우기)
        drawContext.canvas.nativeCanvas.drawColor(AndroidColor.TRANSPARENT, PorterDuff.Mode.CLEAR)

        detections.forEach { detection ->
            val box = detection.boundingBox

            // PreviewView의 스케일 타입에 따라 좌표 변환 로직이 달라질 수 있음
            // FILL_CENTER (기본값) 스케일 타입에 맞춰 좌표를 변환
            val normalizedBox = when (previewView.scaleType) {
                PreviewView.ScaleType.FILL_CENTER -> {
                    val previewWidth = previewView.width.toFloat()
                    val previewHeight = previewView.height.toFloat()
                    val targetAspectRatio = previewWidth / previewHeight
                    val imageAspectRatio =
                        detection.boundingBox.width / detection.boundingBox.height

                    val scaledWidth: Float
                    val scaledHeight: Float
                    val offsetX: Float
                    val offsetY: Float

                    if (targetAspectRatio > imageAspectRatio) { // Preview가 이미지보다 가로가 김 -> 세로 맞춤
                        scaledHeight = previewHeight
                        scaledWidth = previewHeight * imageAspectRatio
                        offsetX = (previewWidth - scaledWidth) / 2
                        offsetY = 0f
                    } else { // Preview가 이미지보다 세로가 김 -> 가로 맞춤
                        scaledWidth = previewWidth
                        scaledHeight = previewWidth / imageAspectRatio
                        offsetX = 0f
                        offsetY = (previewHeight - scaledHeight) / 2
                    }

                    // TFLite 모델 결과는 항상 0-1 스케일 (320x320)로 나옴
                    // 이 스케일을 PreviewView의 실제 픽셀 값으로 변환
                    val x1 = box.left * scaledWidth + offsetX
                    val y1 = box.top * scaledHeight + offsetY
                    val x2 = box.right * scaledWidth + offsetX
                    val y2 = box.bottom * scaledHeight + offsetY

                    RectF(x1, y1, x2, y2)
                }
                // 다른 ScaleType에 대한 처리도 필요할 수 있으나, 일단 FILL_CENTER만 고려
                else -> {
                    RectF(
                        box.left * previewView.width,
                        box.top * previewView.height,
                        box.right * previewView.width,
                        box.bottom * previewView.height
                    )
                }
            }

            // Canvas에 그릴 실제 좌표 (Canvas의 크기 = 화면 크기)
            val drawRect = RectF(
                normalizedBox.left * scaleX,
                normalizedBox.top * scaleY,
                normalizedBox.right * scaleX,
                normalizedBox.bottom * scaleY
            )

            // 바운딩 박스 그리기
            drawContext.canvas.nativeCanvas.drawRoundRect(
                drawRect,
                24f, 24f,
                Paint().apply {
                    shader = LinearGradient(
                        drawRect.left, drawRect.top, drawRect.right, drawRect.bottom,
                        AndroidColor.CYAN, AndroidColor.BLUE, TileMode.MIRROR
                    )
                    style = Paint.Style.STROKE
                    strokeWidth = 6f
                }
            )

            // 라벨 배경 그리기
            val textPaint = Paint().apply {
                color = AndroidColor.WHITE
                textSize = 30f
                typeface = Typeface.DEFAULT_BOLD
            }
            val textBackgroundPaint = Paint().apply {
                color = AndroidColor.CYAN
                style = Paint.Style.FILL
            }

            val text = detection.label.uppercase()
            val textWidth = textPaint.measureText(text)
            val textHeight = textPaint.descent() - textPaint.ascent()

            drawContext.canvas.nativeCanvas.drawRect(
                drawRect.left,
                drawRect.top - textHeight - 10,
                drawRect.left + textWidth + 20,
                drawRect.top,
                textBackgroundPaint
            )

            // 라벨 텍스트 그리기
            drawContext.canvas.nativeCanvas.drawText(
                text,
                drawRect.left + 10,
                drawRect.top - 10, // 텍스트를 박스 위에 위치
                textPaint
            )
        }
    }
}
