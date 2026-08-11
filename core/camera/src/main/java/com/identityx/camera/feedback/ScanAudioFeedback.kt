package com.identityx.camera.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Plays a short confirmation beep when a barcode is successfully scanned.
 *
 * Uses [ToneGenerator] — no audio file assets required.
 * Call [release] when the scan screen is dismissed to free the audio stream.
 */
class ScanAudioFeedback(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, BEEP_VOLUME)
        } catch (e: RuntimeException) {
            // ToneGenerator unavailable (e.g. audio focus denied) — fail silently
        }
    }

    /**
     * Plays a short confirmation beep (~100ms).
     * No-op if the audio stream is unavailable.
     */
    fun playBeep() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, BEEP_DURATION_MS)
    }

    /** Release audio resources. Must be called when the scanner is no longer in use. */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }

    companion object {
        private const val BEEP_VOLUME      = 80   // 0–100
        private const val BEEP_DURATION_MS = 100
    }
}
