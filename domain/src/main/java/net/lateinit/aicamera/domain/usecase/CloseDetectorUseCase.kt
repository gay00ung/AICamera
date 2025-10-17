package net.lateinit.aicamera.domain.usecase

import net.lateinit.aicamera.domain.repository.ObjectDetectorRepository

class CloseDetectorUseCase(
    private val repository: ObjectDetectorRepository
) {
    operator fun invoke() {
        repository.close()
    }
}
