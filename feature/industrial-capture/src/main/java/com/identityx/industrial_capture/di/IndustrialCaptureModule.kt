package com.identityx.industrial_capture.di

import android.content.Context
import com.identityx.camera.feedback.ScanAudioFeedback
import com.identityx.camera.feedback.ScanHapticFeedback
import com.identityx.industrial_capture.data.ProductRepositoryImpl
import com.identityx.industrial_capture.domain.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IndustrialCaptureBindsModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository
}

@Module
@InstallIn(SingletonComponent::class)
object IndustrialCaptureProvidesModule {

    @Provides
    @Singleton
    fun provideScanAudioFeedback(
        @ApplicationContext context: Context
    ): ScanAudioFeedback = ScanAudioFeedback(context)

    @Provides
    @Singleton
    fun provideScanHapticFeedback(
        @ApplicationContext context: Context
    ): ScanHapticFeedback = ScanHapticFeedback(context)
}
