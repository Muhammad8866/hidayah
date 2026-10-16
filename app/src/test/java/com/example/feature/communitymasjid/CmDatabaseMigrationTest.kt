package com.example.feature.communitymasjid

import androidx.test.core.app.ApplicationProvider
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.example.feature.communitymasjid.data.local.CmDatabase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CmDatabaseMigrationTest {
    @Test
    fun upgradePreservesCommunityDataAndRemovesPreviousDiscoveryCache() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(ApplicationProvider.getApplicationContext())
                .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE cm_osm_mosques (docId TEXT PRIMARY KEY, isUserAdded INTEGER NOT NULL, name TEXT)")
                        db.execSQL("CREATE TABLE cm_osm_query (id INTEGER PRIMARY KEY, latitude REAL, longitude REAL, fetchedAtMs INTEGER)")
                        db.execSQL("CREATE TABLE cm_community_records (docId TEXT PRIMARY KEY, currentJson TEXT)")
                        db.execSQL("INSERT INTO cm_osm_mosques VALUES ('user_abc', 1, 'Community masjid'), ('node_42', 0, 'Previous provider')")
                        db.execSQL("INSERT INTO cm_osm_query VALUES (0, 28.6, 77.2, 123)")
                        db.execSQL("INSERT INTO cm_community_records VALUES ('user_abc', 'saved timing'), ('google_place', 'google timing'), ('node_42', 'old timing')")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                }).build()
        )
        helper.use {
            val db = it.writableDatabase
            CmDatabase.MIGRATION_3_4.migrate(db)
            db.query("SELECT docId, name FROM cm_cached_mosques").use { rows ->
                assertEquals(1, rows.count)
                rows.moveToFirst()
                assertEquals("user_abc", rows.getString(0))
                assertEquals("Community masjid", rows.getString(1))
            }
            db.query("SELECT currentJson FROM cm_community_records WHERE docId = 'user_abc'").use { rows ->
                rows.moveToFirst()
                assertEquals("saved timing", rows.getString(0))
            }
            db.query("SELECT COUNT(*) FROM cm_community_records").use { rows ->
                rows.moveToFirst()
                assertEquals(2, rows.getInt(0))
            }
            db.query("SELECT latitude FROM cm_nearby_query").use { rows ->
                rows.moveToFirst()
                assertEquals(28.6, rows.getDouble(0), 0.001)
            }
        }
    }
}
