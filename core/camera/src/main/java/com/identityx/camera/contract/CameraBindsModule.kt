package com.identityx.camera.contract

import com.identityx.camera.controller.CameraXScannerEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CameraBindsModule {

    @Binds
    @Singleton
    abstract fun bindBarcodeScannerEngine(
        impl: CameraXScannerEngine
    ): BarcodeScannerEngine
}
