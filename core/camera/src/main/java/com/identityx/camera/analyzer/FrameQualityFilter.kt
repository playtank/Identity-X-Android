package com.identityx.camera.analyzer

import android.os.SystemClock

/**
 * Filters out low-quality or too-frequent barcode scan results.
 *
 * Two guard mechanisms:
 * 1. **Debounce** — ignores repeated results within [debounceMs] milliseconds.
 *    Prevents the same barcode from being reported dozens of times per second.
 * 2. **Duplicate suppression** — ignores a result that matches the last accepted value,
 *    resetting only after [resetAfterMs] of silence.
 */
class FrameQualityFilter(
    private val debounceMs: Long = 1_500L,
    private val resetAfterMs: Long = 5_000L
) {
    private var lastAcceptedValue: String? = null
    private var lastAcceptedTime: Long = 0L

    /**
     * Returns true if [value] should be passed to the consumer.
     * Thread-safe via @Synchronized.
     */
    @Synchronized
    fun accept(value: String): Boolean {
        val now = SystemClock.elapsedRealtime()

        // Reset stale last-accepted value after silence period
        if (now - lastAcceptedTime > resetAfterMs) {
            lastAcceptedValue = null
        }

        // Debounce: reject if within cooldown window
        if (now - lastAcceptedTime < debounceMs) return false

        // Duplicate suppression: reject if same as last accepted
        if (value == lastAcceptedValue) return false

        lastAcceptedValue = value
        lastAcceptedTime = now
        return true
    }

    /** Resets filter state — call when the scan screen is dismissed. */
    fun reset() {
        lastAcceptedValue = null
        lastAcceptedTime = 0L
    }
}
