package com.identityx.edge_sync.di

import com.identityx.android.core.network.industrial.IndustrialKtorApi
import com.identityx.edge_sync.domain.AssetRepository
import com.identityx.edge_sync.domain.AssetRepositoryImpl
import com.identityx.local.edge.AssetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides edge-sync domain dependencies.
 *
 * [IndustrialKtorApi] is provided by NetworkModule in :core:network
 * (backed by a mock engine until the real ingestion endpoint is available).
 */
@Module
@InstallIn(SingletonComponent::class)
class EdgeSyncModule {

    @Provides
    @Singleton
    fun provideAssetRepository(
        assetDao: AssetDao,
        industrialKtorApi: IndustrialKtorApi
    ): AssetRepository {
        return AssetRepositoryImpl(assetDao, industrialKtorApi)
    }
}
