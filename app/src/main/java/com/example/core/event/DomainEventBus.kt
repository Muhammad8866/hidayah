package com.example.core.event

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance

/**
 * Clean In-Memory Publish-Subscribe Domain Event Bus.
 * Decouples cross-context communications with zero circular dependencies.
 */
object DomainEventBus {
    val eventFlow: MutableSharedFlow<DomainEvent> = MutableSharedFlow(extraBufferCapacity = 64)
    val events: SharedFlow<DomainEvent> = eventFlow.asSharedFlow()

    suspend fun publish(event: DomainEvent) {
        eventFlow.emit(event)
    }

    fun tryPublish(event: DomainEvent): Boolean {
        return eventFlow.tryEmit(event)
    }

    inline fun <reified T : DomainEvent> observe(): Flow<T> {
        return eventFlow.filterIsInstance<T>()
    }
}
