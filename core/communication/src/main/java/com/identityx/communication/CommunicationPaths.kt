package com.identityx.communication

/**
 * Global contract paths for cross-device signaling over the Google Data Layer API.
 * Ensures strict alignment between handheld and wearable nodes during communication.
 */
object CommunicationPaths {

    /**
     * Handheld -> Wearable: Broadcasts successful CIAM authentication status,
     * delivering the validated user payload and session tokens to the edge runtime.
     */
    const val AUTH_STATUS_SUCCESS = "/auth/status/success"

    /**
     * Wearable -> Handheld: Transports raw bi-directional industrial telemetry data,
     * including image captures or voice logs gathered at the field operations site.
     */
    const val ASSET_UPLOAD_PAYLOAD = "/upload/payload"

    /**
     * Handheld -> Wearable: Reverses acknowledgement (ACK) signal indicating the
     * handheld gateway has successfully offloaded the asset payload to the cloud ERP.
     */
    const val SYNC_ACKNOWLEDGEMENT = "/sync/ack"
}