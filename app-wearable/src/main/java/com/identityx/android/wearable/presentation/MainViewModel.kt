package com.identityx.android.wearable.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.identityx.android.wearable.communication.CommunicationPaths
import com.identityx.android.wearable.communication.WearableMessageSender
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val messageSender = WearableMessageSender(application)

    fun sendAssetPayload() {
        val mockAssetPayload = ByteArray(100) { i -> i.toByte() }
        viewModelScope.launch {
            try {
                messageSender.sendMessageToHandheld(
                    path = CommunicationPaths.ASSET_UPLOAD_PAYLOAD,
                    payload = mockAssetPayload
                )
                Log.d(TAG, "Payload sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Send failed: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}
