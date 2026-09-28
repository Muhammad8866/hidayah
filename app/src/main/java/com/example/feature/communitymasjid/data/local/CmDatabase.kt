package com.example.feature.communitymasjid.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

/*
 * Community Masjid keeps its own database file ("community_masjid.db") so a schema change,
 * migration failure or corruption here can never touch the main app database.
 */

/** Cached nearby list: OSM masjids plus community-added ones ([isUserAdded]). */
@Entity(tableName = "cm_osm_mosques")
data class CmOsmMosqueEntity(
    @PrimaryKey val docId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val fetchedAtMs: Long,
    val isUserAdded: Boolean = false,
    val addedByUid: String? = null
)

/** Singleton row describing where the cached OSM list was fetched from. */
@Entity(tableName = "cm_osm_query")
data class CmOsmQueryEntity(
    @PrimaryKey val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val fetchedAtMs: Long
)

@Entity(tableName = "cm_community_records")
data class CmCommunityRecordEntity(
    @PrimaryKey val docId: String,
    val exists: Boolean,
    val currentJson: String?,
    val currentVersion: Long,
    val updatedAtMs: Long?,
    val updatedByUid: String?,
    val timingUp: Int,
    val timingDown: Int,
    val upVotes: Int,
    val downVotes: Int,
    val fetchedAtMs: Long
)

/** This device's own submissions, used for local rate limiting. */
@Entity(tableName = "cm_local_actions")
data class CmLocalActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val mosqueDocId: String,
    val suggestionId: String,
    val atMs: Long
)

@Dao
interface CmDao {
    @Query("SELECT * FROM cm_osm_mosques")
    suspend fun osmMosques(): List<CmOsmMosqueEntity>

    @Query("SELECT * FROM cm_osm_mosques WHERE docId = :docId LIMIT 1")
    suspend fun osmMosque(docId: String): CmOsmMosqueEntity?

    @Query("SELECT * FROM cm_osm_query WHERE id = 0")
    suspend fun osmQuery(): CmOsmQueryEntity?

    @Query("DELETE FROM cm_osm_mosques")
    suspend fun clearOsmMosques()

    @Query("DELETE FROM cm_osm_mosques WHERE docId = :docId")
    suspend fun deleteOsmMosque(docId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOsmMosques(items: List<CmOsmMosqueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOsmQuery(query: CmOsmQueryEntity)

    @Query("SELECT * FROM cm_community_records WHERE docId IN (:docIds)")
    suspend fun records(docIds: List<String>): List<CmCommunityRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecords(items: List<CmCommunityRecordEntity>)

    @Insert
    suspend fun insertAction(action: CmLocalActionEntity)

    @Query("SELECT * FROM cm_local_actions WHERE atMs >= :sinceMs ORDER BY atMs DESC")
    suspend fun actionsSince(sinceMs: Long): List<CmLocalActionEntity>

    @Query("DELETE FROM cm_local_actions WHERE atMs < :beforeMs")
    suspend fun pruneActions(beforeMs: Long)
}

@Database(
    entities = [
        CmOsmMosqueEntity::class,
        CmOsmQueryEntity::class,
        CmCommunityRecordEntity::class,
        CmLocalActionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CmDatabase : RoomDatabase() {
    abstract fun dao(): CmDao

    companion object {
        @Volatile private var instance: CmDatabase? = null

        fun get(context: Context): CmDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CmDatabase::class.java,
                "community_masjid.db"
            ).fallbackToDestructiveMigration(dropAllTables = true).build().also { instance = it }
        }
    }
}
