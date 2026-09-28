package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OutboxMutationType {
    SYNC_HIFZ_RECORD,
    SYNC_BOOKMARK
}

enum class OutboxStatus {
    PENDING,
    IN_FLIGHT,
    FAILED_RETRYABLE,
    FAILED_TERMINAL,
    COMPLETED
}

@Entity(tableName = "sync_outbox")
data class SyncOutboxEntity(
    @PrimaryKey val id: String,
    val mutationType: OutboxMutationType,
    val payloadJson: String,
    val retryCount: Int = 0,
    val maxRetries: Int = 5,
    val status: OutboxStatus = OutboxStatus.PENDING,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val lastAttemptEpochMs: Long? = null,
    val errorMessage: String? = null
)
