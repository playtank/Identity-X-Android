package com.identityx.local.di

import android.content.Context
import androidx.room.Room
import com.identityx.local.AppDatabase
import com.identityx.local.EncryptedTokenProvider
import com.identityx.local.domain.TokenProvider
import com.identityx.local.domain.UserPreferencesProvider
import com.identityx.local.edge.AssetDao
import com.identityx.local.energy.EnergyAccountDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataModule {

    /**
     * Constructs EncryptedTokenProvider explicitly.
     * We use @Provides instead of @Binds because EncryptedTokenProvider has no
     * @Inject constructor — it lives in :shared:local which has no Hilt plugin.
     */
    @Provides
    @Singleton
    fun provideEncryptedTokenProvider(
        @ApplicationContext context: Context
    ): EncryptedTokenProvider = EncryptedTokenProvider(context)

    @Provides
    @Singleton
    fun provideTokenProvider(impl: EncryptedTokenProvider): TokenProvider = impl

    @Provides
    @Singleton
    fun provideUserPreferencesProvider(impl: EncryptedTokenProvider): UserPreferencesProvider = impl

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideEnergyAccountDao(db: AppDatabase): EnergyAccountDao = db.energyAccountDao()

    @Provides
    fun provideAssetDao(db: AppDatabase): AssetDao = db.assetDao()
}
