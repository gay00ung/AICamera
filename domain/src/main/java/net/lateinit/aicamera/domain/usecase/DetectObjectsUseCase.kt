package net.lateinit.aicamera.domain.usecase

import kotlinx.coroutines.flow.Flow
import net.lateinit.aicamera.domain.model.DetectionResult
import net.lateinit.aicamera.domain.repository.ObjectDetectorRepository

class DetectObjectsUseCase(
    private val repository: ObjectDetectorRepository
) {
    operator fun invoke(bitmap: Any, imageRotation: Int): Flow<Pair<List<DetectionResult>, Long>> {
        return repository.detect(bitmap, imageRotation)
    }
}
