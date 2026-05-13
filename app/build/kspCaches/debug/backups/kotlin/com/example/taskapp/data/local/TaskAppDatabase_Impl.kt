package com.example.taskapp.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TaskAppDatabase_Impl : TaskAppDatabase() {
  private val _taskListDao: Lazy<TaskListDao> = lazy {
    TaskListDao_Impl(this)
  }

  private val _taskItemDao: Lazy<TaskItemDao> = lazy {
    TaskItemDao_Impl(this)
  }

  private val _notificationSettingDao: Lazy<NotificationSettingDao> = lazy {
    NotificationSettingDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(6, "f197cf3744b717da5734a4147d98a5ce", "34c502b3923d898a30181a408445d7cf") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `task_lists` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `type` TEXT NOT NULL, `textContent` TEXT, `colorArgb` INTEGER, `isDeleted` INTEGER NOT NULL, `isArchived` INTEGER NOT NULL, `deletedAt` INTEGER, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `task_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `listId` INTEGER NOT NULL, `text` TEXT NOT NULL, `isChecked` INTEGER NOT NULL, `position` INTEGER NOT NULL, FOREIGN KEY(`listId`) REFERENCES `task_lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_task_items_listId` ON `task_items` (`listId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `notification_settings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `listId` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL, `frequency` TEXT NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `weekDaysMask` INTEGER NOT NULL, `oneTimeEpochMillis` INTEGER NOT NULL, `intervalValue` INTEGER NOT NULL, `intervalUnit` TEXT NOT NULL, FOREIGN KEY(`listId`) REFERENCES `task_lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_notification_settings_listId` ON `notification_settings` (`listId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f197cf3744b717da5734a4147d98a5ce')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `task_lists`")
        connection.execSQL("DROP TABLE IF EXISTS `task_items`")
        connection.execSQL("DROP TABLE IF EXISTS `notification_settings`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsTaskLists: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTaskLists.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskLists.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskLists.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskLists.put("textContent", TableInfo.Column("textContent", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskLists.put("colorArgb", TableInfo.Column("colorArgb", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskLists.put("isDeleted", TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskLists.put("isArchived", TableInfo.Column("isArchived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskLists.put("deletedAt", TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskLists.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTaskLists: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTaskLists: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoTaskLists: TableInfo = TableInfo("task_lists", _columnsTaskLists, _foreignKeysTaskLists, _indicesTaskLists)
        val _existingTaskLists: TableInfo = read(connection, "task_lists")
        if (!_infoTaskLists.equals(_existingTaskLists)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |task_lists(com.example.taskapp.data.local.TaskListEntity).
              | Expected:
              |""".trimMargin() + _infoTaskLists + """
              |
              | Found:
              |""".trimMargin() + _existingTaskLists)
        }
        val _columnsTaskItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTaskItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskItems.put("listId", TableInfo.Column("listId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskItems.put("text", TableInfo.Column("text", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskItems.put("isChecked", TableInfo.Column("isChecked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskItems.put("position", TableInfo.Column("position", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTaskItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysTaskItems.add(TableInfo.ForeignKey("task_lists", "CASCADE", "NO ACTION", listOf("listId"), listOf("id")))
        val _indicesTaskItems: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTaskItems.add(TableInfo.Index("index_task_items_listId", false, listOf("listId"), listOf("ASC")))
        val _infoTaskItems: TableInfo = TableInfo("task_items", _columnsTaskItems, _foreignKeysTaskItems, _indicesTaskItems)
        val _existingTaskItems: TableInfo = read(connection, "task_items")
        if (!_infoTaskItems.equals(_existingTaskItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |task_items(com.example.taskapp.data.local.TaskItemEntity).
              | Expected:
              |""".trimMargin() + _infoTaskItems + """
              |
              | Found:
              |""".trimMargin() + _existingTaskItems)
        }
        val _columnsNotificationSettings: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsNotificationSettings.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotificationSettings.put("listId", TableInfo.Column("listId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotificationSettings.put("isEnabled", TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotificationSettings.put("frequency", TableInfo.Column("frequency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotificationSettings.put("hour", TableInfo.Column("hour", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotificationSettings.put("minute", TableInfo.Column("minute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotificationSettings.put("weekDaysMask", TableInfo.Column("weekDaysMask", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotificationSettings.put("oneTimeEpochMillis", TableInfo.Column("oneTimeEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotificationSettings.put("intervalValue", TableInfo.Column("intervalValue", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotificationSettings.put("intervalUnit", TableInfo.Column("intervalUnit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysNotificationSettings: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysNotificationSettings.add(TableInfo.ForeignKey("task_lists", "CASCADE", "NO ACTION", listOf("listId"), listOf("id")))
        val _indicesNotificationSettings: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesNotificationSettings.add(TableInfo.Index("index_notification_settings_listId", true, listOf("listId"), listOf("ASC")))
        val _infoNotificationSettings: TableInfo = TableInfo("notification_settings", _columnsNotificationSettings, _foreignKeysNotificationSettings, _indicesNotificationSettings)
        val _existingNotificationSettings: TableInfo = read(connection, "notification_settings")
        if (!_infoNotificationSettings.equals(_existingNotificationSettings)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |notification_settings(com.example.taskapp.data.local.NotificationSettingEntity).
              | Expected:
              |""".trimMargin() + _infoNotificationSettings + """
              |
              | Found:
              |""".trimMargin() + _existingNotificationSettings)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "task_lists", "task_items", "notification_settings")
  }

  public override fun clearAllTables() {
    super.performClear(true, "task_lists", "task_items", "notification_settings")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(TaskListDao::class, TaskListDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TaskItemDao::class, TaskItemDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(NotificationSettingDao::class, NotificationSettingDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun taskListDao(): TaskListDao = _taskListDao.value

  public override fun taskItemDao(): TaskItemDao = _taskItemDao.value

  public override fun notificationSettingDao(): NotificationSettingDao = _notificationSettingDao.value
}
