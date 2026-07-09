package com.identityx.edge_sync.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

/**
 * Schedules and observes the [OfflineSyncWorker] via WorkManager.
 *
 * Uses a unique work name to ensure only one sync job is ever queued at a time.
 * Replaces stale/failed jobs automatically to prevent zombie tasks blocking the queue.
 */
class OperationalManager(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val SYNC_WORK_NAME = "industrial_offline_sync_work"
    }

    /**
     * Enqueues a one-time sync job with network connectivity constraint and exponential backoff.
     * Replaces any previously failed or cancelled job; keeps running jobs as-is.
     */
    fun triggerOfflineSync() {
        val workInfos = workManager.getWorkInfosForUniqueWork(SYNC_WORK_NAME).get()
        val currentState = workInfos.firstOrNull()?.state

        // Replace stale terminal states; keep a healthy running/enqueued job
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
