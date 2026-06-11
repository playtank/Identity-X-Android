package com.identityx.database.di

import android.content.Context
import androidx.room.Room
import com.identityx.database.AppDatabase
import com.identityx.database.energy.EnergyAccountDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
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