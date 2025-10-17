package net.lateinit.aicamera.domain.di

import dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.lateinit.aicamera.domain.repository.ObjectDetectorRepository
import net.lateinit.aicamera.domain.usecase.CloseDetectorUseCase
import net.lateinit.aicamera.domain.usecase.DetectObjectsUseCase
import net.lateinit.aicamera.domain.usecase.GetErrorStreamUseCase
import net.lateinit.aicamera.domain.usecase.SetupDetectorUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideSetupDetectorUseCase(repository: ObjectDetectorRepository): SetupDetectorUseCase {
        return SetupDetectorUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDetectObjectsUseCase(repository: ObjectDetectorRepository): DetectObjectsUseCase {
        return DetectObjectsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetErrorStreamUseCase(repository: ObjectDetectorRepository): GetErrorStreamUseCase {
        return GetErrorStreamUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCloseDetectorUseCase(repository: ObjectDetectorRepository): CloseDetectorUseCase {
        return CloseDetectorUseCase(repository)
    }
}
