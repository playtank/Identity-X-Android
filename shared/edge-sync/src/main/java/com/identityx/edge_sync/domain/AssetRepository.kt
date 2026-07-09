package com.identityx.edge_sync.domain

import kotlinx.coroutines.flow.Flow

interface AssetRepository {

    /**
     * Persists a captured asset to local storage immediately (offline-first principle).
     * Local storage is the single source of truth — network sync happens later via WorkManager.
     */
    suspend fun saveCapturedAssetLocal(id: String, timestamp: Long, rawBytes: ByteArray): Boolean

    /**
     * Returns all assets that have not yet been synced to the remote server.
     * Called by [com.identityx.edge_sync.sync.OfflineSyncWorker] when network is available.
     */
    suspend fun getUnsyncedPayloads(): List<AssetTransaction>

    /**
     * Uploads a single pending asset to the remote endpoint and marks it as synced on success.
     * @return true if the server acknowledged the upload; false on server error or network failure.
     */
    suspend fun uploadPendingAsset(transaction: AssetTransaction): Boolean

    /**
     * Reactive stream of the current pending upload count.
     * Useful for showing "N files pending upload" in the dashboard UI.
     */
    fun observePendingCount(): Flow<Int>
}

/**
 * Shared domain entity representing a captured asset transaction.
 * Decoupled from Room entity to keep the domain layer platform-agnostic.
 */
data class AssetTransaction(
    val id: String,
    val timestamp: Long,
    val payload: ByteArray,
    val isSynced: Boolean
)
