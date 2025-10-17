package net.lateinit.aicamera.ui.screen

import androidx.camera.view.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.viewinterop.*

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    onPreviewViewCreated: (PreviewView) -> Unit
) {
    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context).apply {
                this.scaleType = scaleType
            }
            onPreviewViewCreated(previewView)
            previewView
        },
        modifier = modifier.fillMaxSize()
    )
}
