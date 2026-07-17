package com.identityx.local.edge

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    /**
     * Commits a newly captured asset to local disk immediately.
     * Overwrites old records if a duplicate ID collision occurs to maintain idempotency.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(entity: AssetEntity)

    /**
     * Fetches all unsynced entries sequentially for processing by the background worker.
     */
    @Query("SELECT * FROM asset_transactions WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedEntities(): List<AssetEntity>

    /**
     * Updates individual record state upon successful remote receipt acknowledgment.
     */
    @Query("UPDATE asset_transactions SET isSynced = 1 WHERE id = :assetId")
    suspend fun markAsSynced(assetId: String)

    /**
     * Safely deletes records that have been successfully offloaded to reclaim device disk space.
     */
    @Query("DELETE FROM asset_transactions WHERE id = :assetId AND isSynced = 1")
    suspend fun clearSyncedRecord(assetId: String)

    /**
     * Supplies a continuous stream of the current pending queue size to update dashboard metrics reactively.
     */
    @Query("SELECT COUNT(id) FROM asset_transactions WHERE isSynced = 0")
    fun observeUnsyncedCount(): Flow<Int>
}