package net.lateinit.aicamera.ui.viewmodel

import android.graphics.*
import android.util.*
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.lateinit.aicamera.domain.usecase.*
import net.lateinit.aicamera.ui.state.*
import javax.inject.*

@HiltViewModel
class MainViewModel @Inject constructor(
    private val setupDetectorUseCase: SetupDetectorUseCase,
    private val detectObjectsUseCase: DetectObjectsUseCase,
    private val getErrorStreamUseCase: GetErrorStreamUseCase,
    private val closeDetectorUseCase: CloseDetectorUseCase
) : ViewModel() {
    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        setupDetector()
        observeErrors()
    }

    // 카메라 권한 결과를 처리
    fun onCameraPermissionResult(isGranted: Boolean) {
        _uiState.value = _uiState.value.copy(isCameraPermissionGranted = isGranted)
    }

    // AI 모델 초기화
    private fun setupDetector() {
        viewModelScope.launch {
            try {
                setupDetectorUseCase(
                    modelName = "model.tflite",
                    scoreThreshold = 0.5f,
                    maxResults = 3
                )
                _uiState.value = _uiState.value.copy(isDetectorReady = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "모델 설정 실패: ${e.message}")
            }
        }
    }

    // 에러 스트림 감시
    private fun observeErrors() {
        viewModelScope.launch {
            getErrorStreamUseCase()
                .catch { e ->
                    Log.e(TAG, "Error stream failed", e)
                }
                .collectLatest { errorMessage ->
                    _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
                }
        }
    }

    // 카메라 프레임(Bitmap)을 받아 AI 분석 요청
    fun onBitmapAnalyzed(bitmap: Bitmap, rotation: Int) {
        if (!_uiState.value.isDetectorReady) {
            return
        }
        viewModelScope.launch {
            detectObjectsUseCase(bitmap, rotation)
                .flowOn(Dispatchers.Default) // AI 추론은 Default 스레드에서 실행
                .catch { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = "탐지 중 에러: ${e.message}")
                }
                .collectLatest { (detections, inferenceTime) ->
                    // UI 상태 업데이트
                    _uiState.value = _uiState.value.copy(
                        detections = detections,
                        inferenceTime = inferenceTime
                    )
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        closeDetectorUseCase()
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}
