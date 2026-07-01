package com.identityx.login.di

import com.identityx.login.data.LoginDataSource
import com.identityx.login.data.RealLoginDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the active LoginDataSource implementation.
 *
 * To switch back to mock data (e.g. for UI testing or offline dev):
 *   change RealLoginDataSource → MockLoginDataSource on the @Binds line below.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LoginDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindLoginDataSource(
        impl: RealLoginDataSource   // swap to MockLoginDataSource to use mock
    ): LoginDataSource
}
