package com.identityx.edge_sync.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.identityx.edge_sync.domain.AssetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that uploads all pending unsynced assets to the remote server.
 * Scheduled by [OperationalManager] with a CONNECTED network constraint.
 * Returns [Result.retry] with exponential backoff if any upload fails.
 */
@HiltWorker
class OfflineSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: AssetRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingPayloads = repository.getUnsyncedPayloads()
        if (pendingPayloads.isEmpty()) return Result.success()

        var hasAnyFailure = false
        for (payload in pendingPayloads) {
            val success = repository.uploadPendingAsset(payload)
            if (!success) {
                hasAnyFailure = true
                break  // Stop on first failure — remaining items will retry on next run
            }
        }

        return if (hasAnyFailure) Result.retry() else Result.success()
    }
}
