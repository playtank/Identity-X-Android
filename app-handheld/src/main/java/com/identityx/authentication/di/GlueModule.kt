package com.identityx.authentication.di

import com.identityx.authentication.bridge.LoginSessionAdapter
import com.identityx.login.bridge.LoginSessionPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GlueModule {

    @Binds
    @Singleton
    abstract fun bindLoginSessionPort(
        impl: LoginSessionAdapter
    ): LoginSessionPort
}