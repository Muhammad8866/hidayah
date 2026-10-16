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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/*
 * Community Masjid keeps its own database file ("community_masjid.db") so a schema change,
 * migration failure or corruption here can never touch the main app database.
 */

/** Legacy last-query snapshot of community-added masjids. Google results stay in memory. */
@Entity(tableName = "cm_cached_mosques")
data class CmCachedMosqueEntity(
    @PrimaryKey val docId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val fetchedAtMs: Long,
    val isUserAdded: Boolean = false,
    val addedByUid: String? = null
)

/** Singleton row describing where the community list was fetched from. */
@Entity(tableName = "cm_nearby_query")
data class CmNearbyQueryEntity(
    @PrimaryKey val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val fetchedAtMs: Long
)

/** A three-day snapshot of community masjids around one search point. */
@Entity(tableName = "cm_area_cache")
data class CmAreaCacheEntity(
    @PrimaryKey val areaId: String,
    val latitude: Double,
    val longitude: Double,
    val fetchedAtMs: Long,
    val mosquesJson: String
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
    @Query("SELECT * FROM cm_area_cache ORDER BY fetchedAtMs DESC")
    suspend fun areaCaches(): List<CmAreaCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAreaCache(cache: CmAreaCacheEntity)

    @Query("DELETE FROM cm_area_cache WHERE areaId = :areaId")
    suspend fun deleteAreaCache(areaId: String)

    @Query("DELETE FROM cm_area_cache WHERE fetchedAtMs <= :beforeMs")
    suspend fun pruneAreaCaches(beforeMs: Long)

    @Query("SELECT * FROM cm_cached_mosques")
    suspend fun cachedMosques(): List<CmCachedMosqueEntity>

    @Query("SELECT * FROM cm_cached_mosques WHERE docId = :docId LIMIT 1")
    suspend fun cachedMosque(docId: String): CmCachedMosqueEntity?

    @Query("SELECT * FROM cm_nearby_query WHERE id = 0")
    suspend fun nearbyQuery(): CmNearbyQueryEntity?

    @Query("DELETE FROM cm_cached_mosques")
    suspend fun clearCachedMosques()

    @Query("DELETE FROM cm_cached_mosques WHERE docId = :docId")
    suspend fun deleteCachedMosque(docId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedMosques(items: List<CmCachedMosqueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNearbyQuery(query: CmNearbyQueryEntity)

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
        CmCachedMosqueEntity::class,
        CmNearbyQueryEntity::class,
        CmAreaCacheEntity::class,
        CmCommunityRecordEntity::class,
        CmLocalActionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class CmDatabase : RoomDatabase() {
    abstract fun dao(): CmDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cm_osm_mosques RENAME TO cm_cached_mosques")
                db.execSQL("ALTER TABLE cm_osm_query RENAME TO cm_nearby_query")
                db.execSQL("DELETE FROM cm_cached_mosques WHERE isUserAdded = 0")
                db.execSQL("DELETE FROM cm_community_records WHERE docId NOT LIKE 'user_%' AND docId NOT LIKE 'google_%'")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS cm_area_cache (areaId TEXT NOT NULL PRIMARY KEY, latitude REAL NOT NULL, longitude REAL NOT NULL, fetchedAtMs INTEGER NOT NULL, mosquesJson TEXT NOT NULL)")
            }
        }

        @Volatile private var instance: CmDatabase? = null

        fun get(context: Context): CmDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CmDatabase::class.java,
                "community_masjid.db"
            ).addMigrations(MIGRATION_3_4, MIGRATION_4_5).fallbackToDestructiveMigration(dropAllTables = true).build().also { instance = it }
        }
    }
}
