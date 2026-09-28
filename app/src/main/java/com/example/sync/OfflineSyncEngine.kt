package com.example.sync

import com.example.core.result.Result
import com.example.data.local.DeenDao
import com.example.data.local.OutboxStatus
import com.example.data.local.SyncOutboxEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface NetworkAvailabilityPort {
    fun isNetworkAvailable(): Boolean
}

interface RemoteSyncApiPort {
    suspend fun sendMutation(mutation: SyncOutboxEntity): Result<Unit, String>
}

class OfflineSyncEngine(
    private val dao: DeenDao,
    private val networkPort: NetworkAvailabilityPort,
    private val remoteApiPort: RemoteSyncApiPort
) {

    /**
     * Flushes the transactional outbox queue with exponential backoff & terminal failure isolation.
     */
    suspend fun flushOutboxQueue(): Int {
        if (!networkPort.isNetworkAvailable()) return 0

        val pending = dao.getPendingOutboxMutations().first()
        var processedCount = 0

        for (mutation in pending) {
            val inFlightMutation = mutation.copy(
                status = OutboxStatus.IN_FLIGHT,
                lastAttemptEpochMs = System.currentTimeMillis()
            )
            dao.updateOutboxMutation(inFlightMutation)

            when (val result = remoteApiPort.sendMutation(inFlightMutation)) {
                is Result.Success -> {
                    dao.deleteOutboxMutation(mutation.id)
                    processedCount++
                }
                is Result.Failure -> {
                    val nextRetry = mutation.retryCount + 1
                    val newStatus = if (nextRetry >= mutation.maxRetries) {
                        OutboxStatus.FAILED_TERMINAL
                    } else {
                        OutboxStatus.FAILED_RETRYABLE
                    }
                    dao.updateOutboxMutation(
                        mutation.copy(
                            retryCount = nextRetry,
                            status = newStatus,
                            lastAttemptEpochMs = System.currentTimeMillis(),
                            errorMessage = result.error
                        )
                    )
                }
            }
        }
        return processedCount
    }
}
