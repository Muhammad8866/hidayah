package com.example.quran.data

import com.example.core.id.AyahReference
import com.example.quran.domain.hifz.HifzCardState
import com.example.quran.domain.hifz.HifzSpacedRepetitionScheduler
import com.example.quran.domain.port.HifzProgressStorePort

/**
 * In-memory repository implementing HifzProgressStorePort with thread-safe access.
 */
class InMemoryHifzProgressStore : HifzProgressStorePort {

    private val store = mutableMapOf<AyahReference, HifzCardState>()

    override fun getCardState(reference: AyahReference): HifzCardState {
        synchronized(store) {
            return store.getOrPut(reference) {
                HifzCardState(reference = reference)
            }
        }
    }

    override fun saveCardState(cardState: HifzCardState) {
        synchronized(store) {
            store[cardState.reference] = cardState
        }
    }

    override fun getAllDueCards(currentTimestampMs: Long): List<HifzCardState> {
        synchronized(store) {
            return store.values.filter {
                HifzSpacedRepetitionScheduler.isDue(it, currentTimestampMs)
            }
        }
    }
}
