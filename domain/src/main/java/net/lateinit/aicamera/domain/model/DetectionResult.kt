package net.lateinit.aicamera.domain.model

/**
 * 탐지된 객체를 나타내는 순수 도메인 모델
 * (안드로이드 종속성이 없음)
 */
data class DetectionResult(
    val label: String,      // 탐지된 객체의 이름 (예: "person", "cat")
    val score: Float,       // 이 탐지 결과의 신뢰도 점수 (0.0 ~ 1.0)

    // boundingBox는 RectF(android.graphics) 대신 순수 Kotlin 클래스로 표현
    val boundingBox: BoxData
)

/**
 * 안드로이드 RectF를 대체하는 순수 Kotlin 데이터 클래스
 * (좌상단 x,y, 우하단 x,y)
 */
data class BoxData(
    val top: Float,
    val left: Float,
    val bottom: Float,
    val right: Float
)
