package com.identityx.authentication.service

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.identityx.communication.CommunicationPaths
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Core background gateway service that listens for incoming message events from the wearable edge runtime.
 * Managed automatically by Google Play Services, even when the primary application process is killed.
 */
class PhoneGatewayReceiverService : WearableListenerService() {

    // Thread-safe scope to offload message parsing and transient memory-buffering tasks
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Triggered natively by the Wear OS sub-system when a payload packet arrives at this node destination.
     */
    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        // Intercept message events based on predefined protocol contracts
        when (messageEvent.path) {
            CommunicationPaths.AUTH_STATUS_SUCCESS -> {
                serviceScope.launch {
                    val rawPayload = messageEvent.data
                    handleIncomingAuthStatus(rawPayload)
                }
            }
            CommunicationPaths.ASSET_UPLOAD_PAYLOAD -> {
                serviceScope.launch {
                    val rawPayload = messageEvent.data
                    handleIncomingAssetTelemetry(rawPayload)
                }
            }
            else -> {
                // Ignore unexpected signaling paths on the mesh network topology
            }
        }
    }

    /**
     * Decodes and syncs the validated user payload and session tokens received from the edge runtime.
     */
    private suspend fun handleIncomingAuthStatus(payload: ByteArray) {
        // TODO: In Phase 2, unmarshal bytecode and sync with the transient repository layer
        println("Gateway received AUTH_STATUS_SUCCESS payload. Total bytes: ${payload.size}")
    }

    /**
     * Buffers raw binary industrial data (images/voice logs) before dispatching to the storage layer.
     */
    private suspend fun handleIncomingAssetTelemetry(payload: ByteArray) {
        // TODO: Current transient memory-buffered approach. Will stream directly to :core:database in Phase 2
        println("Gateway received ASSET_UPLOAD_PAYLOAD payload. Total bytes: ${payload.size}")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Gracefully cancel all running coroutines to prevent memory leaks during service tearing-down
        serviceScope.cancel()
    }
}