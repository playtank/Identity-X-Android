package com.identityx.edge_sync.sync

import android.content.Context
import androidx.work.*
import com.identityx.local.edge.AssetDao
import com.identityx.local.edge.AssetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Enhanced OperationalManager.
 * Combines your precise unique WorkManager orchestration with an internal,
 * thread-safe Kotlin Channel buffer to process incoming real-time telemetry bursts.
 */
class OperationalManager(
    private val context: Context,
    private val assetDao: AssetDao,                // Injected local Room access database primitive
    private val externalScope: CoroutineScope       // Long-lived application scope for backing daemons
) {

    private val workManager = WorkManager.getInstance(context)

    // 💡 The Ingestion Valve: Sequential buffer to receive incoming strings off real-time streams
    private val telemetryChannel = Channel<String>(Channel.BUFFERED)

    companion object {
        private const val SYNC_WORK_NAME = "industrial_offline_sync_work"
    }

    init {
        // Kick off our background queue listener immediately when this manager initializes
        startChannelConsumerLoop()
    }

    /**
     * Entry-point used directly by your Push BroadcastReceiver or Socket Collectors.
     * Non-blocking: drops raw text strings into the buffer queue instantly.
     */
    suspend fun processIncomingTelemetry(rawTelemetry: String) {
        telemetryChannel.send(rawTelemetry)
    }

    /**
     * Continuous processing daemon loop running safely inside the IO thread pool.
     */
    private fun startChannelConsumerLoop() {
        externalScope.launch(Dispatchers.IO) {
            telemetryChannel.receiveAsFlow().collect { rawData ->
                try {
                    val uniqueAssetId = "TELEMETRY_${UUID.randomUUID()}"

                    // Create your local database entity structure
                    val dbEntity = AssetEntity(
                        id = uniqueAssetId,
                        timestamp = System.currentTimeMillis(),
                        encryptedData = rawData.toByteArray(Charsets.UTF_8),
                        isSynced = false // Tagged explicitly false to catch the SyncWorker's query
                    )

                    // 1. LOCAL PERSISTENCE FIRST: Lock the data securely onto disk
                    assetDao.insertTransaction(dbEntity)

                    // 2. AUTOMATED CHAIN HANDOFF: Invoke your production WorkManager schedule
                    // Because it is committed to Room, your WorkManager will safely find it when online!
                    triggerOfflineSync()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Enqueues a one-time sync job with network connectivity constraint and exponential backoff.
     * Replaces any previously failed or cancelled job; keeps running jobs as-is.
     */
    fun triggerOfflineSync() {
        val workInfos = workManager.getWorkInfosForUniqueWork(SYNC_WORK_NAME).get()
        val currentState = workInfos.firstOrNull()?.state

        val policy = if (
            currentState == null ||
            currentState == WorkInfo.State.FAILED ||
            currentState == WorkInfo.State.CANCELLED ||
            currentState == WorkInfo.State.SUCCEEDED
        ) {
            ExistingWorkPolicy.REPLACE
        } else {
            ExistingWorkPolicy.KEEP
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(SYNC_WORK_NAME, policy, syncRequest)
    }

    /** Observes the sync job state as a Flow for the UI layer. */
    fun observeSyncStatus(): Flow<WorkInfo.State?> =
        workManager.getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map { it.firstOrNull()?.state }
}