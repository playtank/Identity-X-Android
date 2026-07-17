package com.identityx.local.edge

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asset_transactions")
data class AssetEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val encryptedData: ByteArray,
    val isSynced: Boolean
) {
}