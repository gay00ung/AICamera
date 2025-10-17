package net.lateinit.aicamera.data.repository

import android.content.*
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.*
import android.util.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import net.lateinit.aicamera.domain.model.BoxData
import net.lateinit.aicamera.domain.model.DetectionResult
import net.lateinit.aicamera.domain.repository.*
import org.tensorflow.lite.support.image.*
import org.tensorflow.lite.support.image.ops.*
import org.tensorflow.lite.task.core.*
import org.tensorflow.lite.task.vision.detector.*
import javax.inject.Inject
import kotlin.Any
import kotlin.Exception
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlin.to

class ObjectDetectorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ObjectDetectorRepository {
    // TensorFlow Lite 객체 탐지기 인스턴스
    private var objectDetector: ObjectDetector? = null

    // 에러 메시지 전송용 SharedFlow
    private val errorFlow = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override suspend fun setupObjectDetector(
        modelName: String,
        scoreThreshold: Float,
        maxResults: Int
    ) {
        val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
            .setScoreThreshold(scoreThreshold)
            .setMaxResults(maxResults)

        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(4)

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: Exception) {
            val errorMessage = "모델 초기화 실패. assets에 $modelName 파일이 있는지 확인하세요. ${e.message}"
            Log.e(TAG, errorMessage, e)
            errorFlow.tryEmit(errorMessage)
        }
    }

    override fun detect(
        bitmap: Any,
        imageRotation: Int
    ): Flow<Pair<List<DetectionResult>, Long>> = flow {
        if (objectDetector == null) {
            val errorMessage = "객체 탐지기가 초기화되지 않았습니다. setupObjectDetector를 먼저 호출하세요."
            Log.e(TAG, errorMessage)
            errorFlow.tryEmit("Detector가 초기화되지 않았습니다.")
            emit(emptyList<DetectionResult>() to 0L)
            return@flow
        }

        val androidBitmap = bitmap as? Bitmap
        if (androidBitmap == null) {
            val errorMessage = "입력된 객체가 Bitmap이 아닙니다."
            Log.e(TAG, errorMessage)
            errorFlow.tryEmit("잘못된 이미지 타입입니다.")
            emit(emptyList<DetectionResult>() to 0L)
            return@flow
        }

        val inferenceTime = SystemClock.uptimeMillis()

        // 1. 이미지를 모델이 원하는 방향으로 회전
        val imageProcessor =
            ImageProcessor.Builder()
                .add(Rot90Op(-imageRotation / 90)) // 카메라 회전 각도만큼 보정
                .build()

        // 2. 이미지를 TensorImage 형태로 변환
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(androidBitmap))

        // 3. 모델 실행
        val results: List<Detection>? = objectDetector?.detect(tensorImage)

        // 4. 추론 시간 계산
        val totalInferenceTime = SystemClock.uptimeMillis() - inferenceTime

        // 5. TFLite의 Detection 결과를 Domain의 DetectionResult로 변환
        val detectionResults = results?.map {
            it.toDomainModel()
        } ?: emptyList()

        // 6. 결과 방출
        emit(detectionResults to totalInferenceTime)
    }

    override fun getErrorStream(): Flow<String> = errorFlow


    override fun close() {
        objectDetector?.close()
        objectDetector = null
    }

    companion object {
        private const val TAG = "ObjectDetectorRepositoryImpl"
    }
}

/**
 * TFLite의 [Detection] (data layer 모델)을
 * 순수 Kotlin [DetectionResult] (domain layer 모델)로 변환하는 확장 함수
 */
private fun Detection.toDomainModel(): DetectionResult {
    return DetectionResult(
        label = this.categories.firstOrNull()?.label ?: "Unknown",
        score = this.categories.firstOrNull()?.score ?: 0f,
        boundingBox = this.boundingBox.toDomainModel()
    )
}

/**
 * Android의 [RectF] (data layer 모델)를
 * 순수 Kotlin [BoxData] (domain layer 모델)로 변환하는 확장 함수
 */
private fun RectF.toDomainModel(): BoxData {
    return BoxData(
        top = this.top,
        left = this.left,
        bottom = this.bottom,
        right = this.right
    )
}
