package net.lateinit.aicamera.ui.state

import net.lateinit.aicamera.domain.model.DetectionResult

data class MainUiState(
    val isCameraPermissionGranted: Boolean = false, // 카메라 권한 상태
    val isDetectorReady: Boolean = false, // 객체 탐지기 준비 상태
    val detections: List<DetectionResult> = emptyList(), // 탐지된 객체 리스트
    val inferenceTime: Long = 0L, // 추론에 걸린 시간 (ms)
    val errorMessage: String? = null // 에러 메시지
)
