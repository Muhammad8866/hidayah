package com.example.sync

import com.example.core.result.Result
import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeDeenDao : DeenDao {
    val bookmarks = mutableListOf<BookmarkEntity>()
    val hifzList = mutableListOf<HifzProgressEntity>()
    val notes = mutableListOf<UserNoteEntity>()
    val outbox = mutableMapOf<String, SyncOutboxEntity>()

    override fun getAllBookmarks(): Flow<List<BookmarkEntity>> = flowOf(bookmarks)
    override suspend fun insertBookmark(bookmark: BookmarkEntity) { bookmarks.add(bookmark) }
    override suspend fun deleteBookmark(id: String) { bookmarks.removeIf { it.id == id } }

    override fun getAllHifz(): Flow<List<HifzProgressEntity>> = flowOf(hifzList)
    override suspend fun insertHifz(item: HifzProgressEntity) { hifzList.add(item) }

    override fun getAllNotes(): Flow<List<UserNoteEntity>> = flowOf(notes)
    override suspend fun insertNote(note: UserNoteEntity) { notes.add(note) }
    override suspend fun deleteNote(id: String) { notes.removeIf { it.id == id } }

    override fun getPendingOutboxMutations(): Flow<List<SyncOutboxEntity>> =
        flowOf(outbox.values.filter { it.status == OutboxStatus.PENDING || it.status == OutboxStatus.FAILED_RETRYABLE })

    override suspend fun enqueueOutboxMutation(mutation: SyncOutboxEntity) { outbox[mutation.id] = mutation }
    override suspend fun updateOutboxMutation(mutation: SyncOutboxEntity) { outbox[mutation.id] = mutation }
    override suspend fun deleteOutboxMutation(id: String) { outbox.remove(id) }
}

class FakeNetworkPort(var isOnline: Boolean = true) : NetworkAvailabilityPort {
    override fun isNetworkAvailable(): Boolean = isOnline
}

class FakeRemoteSyncApi : RemoteSyncApiPort {
    var shouldFail: Boolean = false
    val sentMutations = mutableListOf<SyncOutboxEntity>()

    override suspend fun sendMutation(mutation: SyncOutboxEntity): Result<Unit, String> {
        return if (shouldFail) {
            Result.Failure("HTTP 503 Service Unavailable")
        } else {
            sentMutations.add(mutation)
            Result.Success(Unit)
        }
    }

}

class OfflineSyncEngineTest {

    private lateinit var dao: FakeDeenDao
    private lateinit var networkPort: FakeNetworkPort
    private lateinit var remoteApi: FakeRemoteSyncApi
    private lateinit var syncEngine: OfflineSyncEngine

    @Before
    fun setUp() {
        dao = FakeDeenDao()
        networkPort = FakeNetworkPort(isOnline = true)
        remoteApi = FakeRemoteSyncApi()
        syncEngine = OfflineSyncEngine(dao, networkPort, remoteApi)
    }

    @Test
    fun testFlushOutbox_SuccessFlow() = runBlocking {
        val mutation = SyncOutboxEntity(
            id = "mut_1",
            mutationType = OutboxMutationType.SYNC_BOOKMARK,
            payloadJson = "{\"bookmark\":\"2:255\"}",
            status = OutboxStatus.PENDING
        )
        dao.enqueueOutboxMutation(mutation)

        val processed = syncEngine.flushOutboxQueue()
        assertEquals(1, processed)
        assertEquals(1, remoteApi.sentMutations.size)
        assertTrue("Completed mutation must be removed from outbox", dao.outbox.isEmpty())
    }

    @Test
    fun testFlushOutbox_RetryableFailure() = runBlocking {
        remoteApi.shouldFail = true
        val mutation = SyncOutboxEntity(
            id = "mut_2",
            mutationType = OutboxMutationType.SYNC_BOOKMARK,
            payloadJson = "{\"bookmark\":\"2:255\"}",
            retryCount = 1,
            maxRetries = 5,
            status = OutboxStatus.PENDING
        )
        dao.enqueueOutboxMutation(mutation)

        val processed = syncEngine.flushOutboxQueue()
        assertEquals(0, processed)

        val updated = dao.outbox["mut_2"]!!
        assertEquals(2, updated.retryCount)
        assertEquals(OutboxStatus.FAILED_RETRYABLE, updated.status)
        assertNotNull(updated.errorMessage)
    }

    @Test
    fun testFlushOutbox_MaxRetriesReachedMarksTerminal() = runBlocking {
        remoteApi.shouldFail = true
        val mutation = SyncOutboxEntity(
            id = "mut_3",
            mutationType = OutboxMutationType.SYNC_BOOKMARK,
            payloadJson = "{\"bookmark\":\"2:255\"}",
            retryCount = 4,
            maxRetries = 5,
            status = OutboxStatus.PENDING
        )
        dao.enqueueOutboxMutation(mutation)

        syncEngine.flushOutboxQueue()

        val updated = dao.outbox["mut_3"]!!
        assertEquals(5, updated.retryCount)
        assertEquals(OutboxStatus.FAILED_TERMINAL, updated.status)
    }

    @Test
    fun testFlushOutbox_OfflinePreservesQueue() = runBlocking {
        networkPort.isOnline = false
        val mutation = SyncOutboxEntity(
            id = "mut_4",
            mutationType = OutboxMutationType.SYNC_BOOKMARK,
            payloadJson = "{\"bookmark\":\"2:255\"}",
            status = OutboxStatus.PENDING
        )
        dao.enqueueOutboxMutation(mutation)

        val processed = syncEngine.flushOutboxQueue()
        assertEquals(0, processed)
        assertEquals(OutboxStatus.PENDING, dao.outbox["mut_4"]!!.status)
    }
}
