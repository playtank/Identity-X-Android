package com.identityx.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.identityx.local.energy.EnergyAccountDao
import com.identityx.local.energy.EnergyAccountEntity

@Database(entities = [EnergyAccountEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun energyAccountDao(): EnergyAccountDao

    // Clears all rows from every @Entity table bound to this database.
    suspend fun clearAllData() {
        clearAllTables()
    }
}
