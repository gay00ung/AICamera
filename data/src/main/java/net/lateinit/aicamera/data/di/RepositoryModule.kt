package net.lateinit.aicamera.data.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.lateinit.aicamera.data.repository.ObjectDetectorRepositoryImpl
import net.lateinit.aicamera.domain.repository.ObjectDetectorRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindObjectDetectorRepository(
        impl: ObjectDetectorRepositoryImpl
    ): ObjectDetectorRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ContextModule {
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}
