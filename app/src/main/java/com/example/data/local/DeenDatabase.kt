package com.example.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val ayahSnippet: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "hifz_progress")
data class HifzProgressEntity(
    @PrimaryKey val key: String, // "surah:ayah"
    val surahNumber: Int,
    val ayahNumber: Int,
    val status: String,
    val confidence: Int,
    val lastReviewed: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_notes")
data class UserNoteEntity(
    @PrimaryKey val id: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface DeenDao {
    @Query("SELECT * FROM user_bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM user_bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: String)

    @Query("SELECT * FROM hifz_progress")
    fun getAllHifz(): Flow<List<HifzProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHifz(item: HifzProgressEntity)

    @Query("SELECT * FROM user_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<UserNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: UserNoteEntity)

    @Query("DELETE FROM user_notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    // Offline Outbox Sync Queries
    @Query("SELECT * FROM sync_outbox WHERE status = 'PENDING' OR status = 'FAILED_RETRYABLE' ORDER BY createdAtEpochMs ASC")
    fun getPendingOutboxMutations(): Flow<List<SyncOutboxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueOutboxMutation(mutation: SyncOutboxEntity)

    @Update
    suspend fun updateOutboxMutation(mutation: SyncOutboxEntity)

    @Query("DELETE FROM sync_outbox WHERE id = :id")
    suspend fun deleteOutboxMutation(id: String)
}

@Database(
    entities = [
        BookmarkEntity::class,
        HifzProgressEntity::class,
        UserNoteEntity::class,
        SyncOutboxEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class DeenDatabase : RoomDatabase() {
    abstract fun deenDao(): DeenDao

    companion object {
        /** Removes the old Masjid tab's data; Community Masjid keeps its own database. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS cached_mosques")
                db.execSQL(
                    "DELETE FROM sync_outbox WHERE mutationType IN " +
                        "('SUBMIT_TIMETABLE_CORRECTION', 'SUBMIT_MOSQUE_ANNOUNCEMENT')"
                )
            }
        }
    }
}
