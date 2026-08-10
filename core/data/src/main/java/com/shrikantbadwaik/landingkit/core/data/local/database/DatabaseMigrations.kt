package com.shrikantbadwaik.landingkit.core.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `checklist_templates` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )""".trimIndent(),
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `checklist_template_items` (
                `template_id` TEXT NOT NULL,
                `sort_order` INTEGER NOT NULL,
                `title` TEXT NOT NULL,
                `detail` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                PRIMARY KEY(`template_id`, `sort_order`),
                FOREIGN KEY(`template_id`) REFERENCES `checklist_templates`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )""".trimIndent(),
        )
        db.execSQL(
            """CREATE INDEX IF NOT EXISTS `checklist_template_items_template_id`
                ON `checklist_template_items` (`template_id`)""".trimIndent(),
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `attachments` (
                `id` TEXT NOT NULL,
                `trip_id` TEXT NOT NULL,
                `display_name` TEXT NOT NULL,
                `mime_type` TEXT NOT NULL,
                `size_bytes` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`trip_id`) REFERENCES `trips`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )""".trimIndent(),
        )
        db.execSQL(
            """CREATE INDEX IF NOT EXISTS `index_attachments_trip_id`
                ON `attachments` (`trip_id`)""".trimIndent(),
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """ALTER TABLE `attachments`
                ADD COLUMN `is_quick_access` INTEGER NOT NULL DEFAULT 0""".trimIndent(),
        )
    }
}
