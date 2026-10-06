package com.identityx.local.edge

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(entity: AssetEntity)

    @Query("SELECT * FROM asset_transactions WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedEntities(): List<AssetEntity>

    @Query("UPDATE asset_transactions SET isSynced = 1 WHERE id = :assetId")
    suspend fun markAsSynced(assetId: String)

    @Query("DELETE FROM asset_transactions WHERE id = :assetId AND isSynced = 1")
    suspend fun clearSyncedRecord(assetId: String)

    @Query("SELECT COUNT(id) FROM asset_transactions WHERE isSynced = 0")
    fun observeUnsyncedCount(): Flow<Int>
}
