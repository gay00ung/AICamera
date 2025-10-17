package net.lateinit.aicamera

import android.*
import android.content.pm.*
import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.activity.result.contract.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.core.content.*
import dagger.hilt.android.*
import net.lateinit.aicamera.ui.screen.*
import net.lateinit.aicamera.ui.viewmodel.*
import java.util.concurrent.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private val viewModel: MainViewModel by viewModels()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            isCameraPermissionGranted = isGranted
            viewModel.onCameraPermissionResult(isGranted)
        }
    private var isCameraPermissionGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        requestCameraPermission()

        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (uiState.isCameraPermissionGranted) {
                    CameraScreen(
                        onImageAnalyzed = { bitmap, rotation ->
                            viewModel.onBitmapAnalyzed(bitmap, rotation)
                        },
                        detections = uiState.detections,
                        inferenceTime = uiState.inferenceTime,
                        cameraExecutor = cameraExecutor
                    )
                } else {
                    PermissionDeniedScreen()
                }

                // 에러 메시지 오버레이
                uiState.errorMessage?.let { message ->
                    AnimatedVisibility(
                        visible = true, // 항상 보이게
                        enter = fadeIn(), exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.TopCenter),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.onCameraPermissionResult(true)
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }
}
