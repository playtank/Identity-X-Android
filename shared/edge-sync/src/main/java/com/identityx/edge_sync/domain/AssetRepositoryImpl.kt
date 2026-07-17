package com.identityx.edge_sync.domain

import com.identityx.android.core.network.industrial.IndustrialKtorApi
import com.identityx.local.edge.AssetDao
import com.identityx.local.edge.AssetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AssetRepositoryImpl(
    private val assetDao: AssetDao,
    private val industrialKtorApi: IndustrialKtorApi
) : AssetRepository {

    /**
     * Writes the asset to local Room storage immediately.
     * isSynced is set to false — the WorkManager job will flip it after a successful upload.
     */
    override suspend fun saveCapturedAssetLocal(
        id: String,
        timestamp: Long,
        rawBytes: ByteArray
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val entity = AssetEntity(
                id            = id,
                timestamp     = timestamp,
                encryptedData = rawBytes,
                isSynced      = false
            )
            assetDao.insertTransaction(entity)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Fetches all unsynced entities from Room and maps them to domain objects. */
    override suspend fun getUnsyncedPayloads(): List<AssetTransaction> =
        withContext(Dispatchers.IO) {
            assetDao.getUnsyncedEntities().map { entity ->
                AssetTransaction(
                    id        = entity.id,
                    timestamp = entity.timestamp,
                    payload   = entity.encryptedData,
                    isSynced  = entity.isSynced
                )
            }
        }

    /**
     * Uploads the asset bytes and marks the record as synced on success.
     * Idempotent — if the Worker retries, a previously synced record will not be re-uploaded
     * because [getUnsyncedPayloads] only returns records where isSynced = false.
     */
    override suspend fun uploadPendingAsset(transaction: AssetTransaction): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val response = industrialKtorApi.uploadAssetBytes(
                    assetId = transaction.id,
                    bytes   = transaction.payload
                )
                if (response.isSuccessful) {
                    assetDao.markAsSynced(transaction.id)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    override fun observePendingCount(): Flow<Int> = assetDao.observeUnsyncedCount()
}
