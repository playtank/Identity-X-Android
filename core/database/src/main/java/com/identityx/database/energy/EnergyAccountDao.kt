package com.identityx.database.energy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EnergyAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(energyAccount: EnergyAccountEntity)

    @Query("SELECT * FROM energy_accounts WHERE id = :id")
    suspend fun getEnergyAccountById(id: Long): EnergyAccountEntity?

    @Query("DELETE FROM energy_accounts WHERE id = :id")
    suspend fun delete(id: Long)
}