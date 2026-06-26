package com.identityx.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.identityx.database.energy.EnergyAccountDao
import com.identityx.database.energy.EnergyAccountEntity

@Database(entities = [EnergyAccountEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun energyAccountDao(): EnergyAccountDao

    // Clears all rows from every @Entity table bound to this database.
    suspend fun clearAllData() {
        clearAllTables()
    }
}
