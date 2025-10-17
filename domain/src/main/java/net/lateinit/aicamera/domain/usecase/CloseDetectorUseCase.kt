package net.lateinit.aicamera.domain.usecase

import net.lateinit.aicamera.domain.repository.ObjectDetectorRepository
import javax.inject.Inject

class CloseDetectorUseCase @Inject constructor(
    private val repository: ObjectDetectorRepository
) {
    operator fun invoke() {
        repository.close()
    }
}
