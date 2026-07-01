package com.identityx.local.di

import android.content.Context
import androidx.room.Room
import com.identityx.local.AppDatabase
import com.identityx.local.EncryptedTokenProvider
import com.identityx.local.domain.TokenProvider
import com.identityx.local.energy.EnergyAccountDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Hilt requires @Binds in an abstract class and @Provides in an object.
// Both are installed in the same component and act as one logical unit.

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalBindsModule {

    @Binds
    @Singleton
    abstract fun bindTokenProvider(
        encryptedTokenProvider: EncryptedTokenProvider
    ): TokenProvider
}

@Module
@InstallIn(SingletonComponent::class)
object LocalProvidesModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideEnergyAccountDao(database: AppDatabase): EnergyAccountDao {
        return database.energyAccountDao()
    }
}
