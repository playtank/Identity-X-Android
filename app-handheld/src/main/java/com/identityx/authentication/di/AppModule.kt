package com.identityx.authentication.di

import android.content.Context
import com.identityx.edge_sync.sync.OperationalManager
import com.identityx.local.edge.AssetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        // A SupervisorJob ensures that if one individual sync task crashes,
        // the entire global application consumer loop doesn't die.
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideOperationalManager(
        @ApplicationContext context: Context,
        assetDao: AssetDao,
        applicationScope: CoroutineScope
    ): OperationalManager {
        // This is instantiated EXACTLY ONCE on launch
        return OperationalManager(context, assetDao, applicationScope)
    }
}