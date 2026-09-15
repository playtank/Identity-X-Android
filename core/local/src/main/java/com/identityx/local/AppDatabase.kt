package com.identityx.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.identityx.local.energy.EnergyAccountDao
import com.identityx.local.energy.EnergyAccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(entities = [EnergyAccountEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun energyAccountDao(): EnergyAccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "energy_accounts"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
    // Clears all rows from every @Entity table bound to this database.
    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            clearAllTables()
        }
    }
}
