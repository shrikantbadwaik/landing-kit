package com.shrikantbadwaik.landingkit.core.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LandingKitDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        LandingKitDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    @Throws(IOException::class)
    fun migration_5_6_preserves_trip_and_adds_not_checked_defaults() {
        helper.createDatabase(TEST_DATABASE, 5).apply {
            execSQL(
                """INSERT INTO trips (
                    id, destination, arrival_at, stay_name, address, local_address,
                    booking_reference, transfer, currency_note, emergency_contact,
                    dietary_phrase, arrival_reminder_enabled
                ) VALUES (
                    'trip', 'Kyoto', '2026-08-22', 'Machiya Stay', '10 Quiet Street',
                    '', 'ABC123', '', '', '', '', 1
                )""".trimIndent(),
            )
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DATABASE,
            6,
            true,
            MIGRATION_5_6,
        ).use { database ->
            database.query(
                """SELECT destination, arrival_reminder_enabled,
                    offline_check_status, offline_checked_at,
                    offline_check_content_fingerprint
                    FROM trips WHERE id = 'trip'""".trimIndent(),
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals("Kyoto", cursor.getString(0))
                assertEquals(1, cursor.getInt(1))
                assertEquals("NotChecked", cursor.getString(2))
                assertEquals(true, cursor.isNull(3))
                assertEquals(true, cursor.isNull(4))
            }
        }
    }

    companion object {
        private const val TEST_DATABASE = "migration-test"
    }
}
