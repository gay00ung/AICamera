package net.lateinit.aicamera.domain.usecase

import kotlinx.coroutines.flow.Flow
import net.lateinit.aicamera.domain.repository.ObjectDetectorRepository
import javax.inject.Inject

class GetErrorStreamUseCase @Inject constructor(
    private val repository: ObjectDetectorRepository
) {
    operator fun invoke(): Flow<String> {
        return repository.getErrorStream()
    }
}
