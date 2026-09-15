package com.identityx.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.identityx.local.edge.AssetDao
import com.identityx.local.edge.AssetEntity
import com.identityx.local.energy.EnergyAccountDao
import com.identityx.local.energy.EnergyAccountEntity

@Database(
    entities = [EnergyAccountEntity::class, AssetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun energyAccountDao(): EnergyAccountDao
    abstract fun assetDao(): AssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }

    suspend fun clearAllData() {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            clearAllTables()
        }
    }
}
