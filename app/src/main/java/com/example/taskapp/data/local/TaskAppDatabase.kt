package com.example.taskapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TaskListEntity::class, TaskItemEntity::class, NotificationSettingEntity::class],
    version = 7,
    exportSchema = false
)
abstract class TaskAppDatabase : RoomDatabase() {
    abstract fun taskListDao(): TaskListDao
    abstract fun taskItemDao(): TaskItemDao
    abstract fun notificationSettingDao(): NotificationSettingDao

    companion object {
        @Volatile private var INSTANCE: TaskAppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE task_lists ADD COLUMN colorArgb INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // version 3 ensured identity hash is updated and we use Int for colors
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE task_lists ADD COLUMN type TEXT NOT NULL DEFAULT 'CHECKLIST'")
                db.execSQL("ALTER TABLE task_lists ADD COLUMN textContent TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE task_lists ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE task_lists ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE task_lists ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE task_lists ADD COLUMN isLocked INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): TaskAppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskAppDatabase::class.java,
                    "taskapp.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
    }
}
