package net.lateinit.aicamera.domain.repository

import kotlinx.coroutines.flow.Flow
import net.lateinit.aicamera.domain.model.DetectionResult

/**
 * 객체 탐지 기능에 대한 명세서 (인터페이스)
 * :app 모듈은 이 인터페이스만 바라보고,
 * :data 모듈이 이 인터페이스를 구현한다.
 */
interface ObjectDetectorRepository {

    /**
     * 객체 탐지기를 초기화한다.
     * @param modelName 모델 파일 이름
     * @param scoreThreshold 신뢰도 임계값
     * @param maxResults 최대 결과 수
     */
    suspend fun setupObjectDetector(
        modelName: String,
        scoreThreshold: Float,
        maxResults: Int
    )

    /**
     * 이미지를 분석하고 결과를 Flow로 반환한다.
     * @param bitmap 분석할 이미지 비트맵
     * @param imageRotation 이미지 회전 각도
     * @return 탐지 결과 리스트와 추론 시간을 담은 Flow
     */
    fun detect(bitmap: Any, imageRotation: Int): Flow<Pair<List<DetectionResult>, Long>>
    // Bitmap(android.graphics) 대신 Any를 사용해 안드로이드 종속성 제거

    /**
     * 에러 발생 시 에러 메시지를 Flow로 반환한다.
     */
    fun getErrorStream(): Flow<String>

    /**
     * 사용한 리소스를 정리한다.
     */
    fun close()
}
