package com.identityx.local.energy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "energy_accounts")
data class EnergyAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val currentPlanName: String,
    val ratePerKwh: Double,
    val currentBillAmount: Double,
    val dueDate: String,
    val usageKwh: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)