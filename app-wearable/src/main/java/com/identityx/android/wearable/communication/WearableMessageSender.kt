package com.identityx.android.wearable.communication

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearableMessageSender(private val context: Context) {

    /**
     * Sends a message to all connected handheld nodes.
     * Throws an exception if no nodes are found or the send fails.
     */
    suspend fun sendMessageToHandheld(path: String, payload: ByteArray) {
        val nodeClient = Wearable.getNodeClient(context)
        val nodes = nodeClient.connectedNodes.await()

        if (nodes.isEmpty()) {
            throw IllegalStateException("No connected handheld nodes found")
        }

        val messageClient = Wearable.getMessageClient(context)
        nodes.forEach { node ->
            messageClient.sendMessage(node.id, path, payload).await()
        }
    }
}
