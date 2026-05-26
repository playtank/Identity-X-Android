package com.identityx.communication

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * Edge-tier message dispatcher responsible for abstracting Google Data Layer APIs
 * and routing raw byte payloads from the wearable device to the mobile gateway.
 */
class WearableMessageSender(context: Context) {

    private val messageClient = Wearable.getMessageClient(context)
    private val capabilityClient = Wearable.getCapabilityClient(context)

    companion object {
        /**
         * Unique capability identifier claimed by the mobile gateway application.
         * Must match the declaration inside the handheld module's wearable.xml.
         */
        private const val HANDHELD_GATEWAY_CAPABILITY = "identityx_handheld_gateway"
    }

    /**
     * Dispatches an arbitrary byte array payload to the nearest reachable handheld gateway.
     * Uses Coroutines Task integration to suspend execution natively during transport.
     *
     * @param path Target communication path acting as the message identifier routing contract.
     * @param payload Raw binary data representing the industrial asset or token bundle.
     * @return True if the message task resolves successfully; false otherwise.
     */
    suspend fun sendPayloadToGateway(path: String, payload: ByteArray): Boolean {
        return try {
            // Query the local BLE/Wi-Fi node mesh network for devices claiming the gateway capability
            val capabilityInfo = capabilityClient.getCapability(
                HANDHELD_GATEWAY_CAPABILITY,
                CapabilityClient.FILTER_REACHABLE
            ).await()

            // Resolve the primary active node connected to this wearable runtime
            val targetNode = capabilityInfo.nodes.firstOrNull()
                ?: throw IOException("No active handheld gateway discovered on the local topology network.")

            // Execute the transport payload dispatch asynchronously
            messageClient.sendMessage(targetNode.id, path, payload).await()
            true
        } catch (e: Exception) {
            // Forward network anomalies to the calling architecture layer to trigger WorkManager backoff policies
            e.printStackTrace()
            false
        }
    }
}