package net.lateinit.aicamera.domain.usecase

import net.lateinit.aicamera.domain.repository.ObjectDetectorRepository

class SetupDetectorUseCase(
    private val repository: ObjectDetectorRepository
) {
    suspend operator fun invoke(
        modelName: String,
        scoreThreshold: Float,
        maxResults: Int
    ) {
        repository.setupObjectDetector(modelName, scoreThreshold, maxResults)
    }
}
