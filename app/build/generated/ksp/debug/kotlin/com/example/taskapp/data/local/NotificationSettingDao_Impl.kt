package com.example.taskapp.`data`.local

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class NotificationSettingDao_Impl(
  __db: RoomDatabase,
) : NotificationSettingDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfNotificationSettingEntity:
      EntityInsertAdapter<NotificationSettingEntity>

  private val __deleteAdapterOfNotificationSettingEntity:
      EntityDeleteOrUpdateAdapter<NotificationSettingEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfNotificationSettingEntity = object : EntityInsertAdapter<NotificationSettingEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `notification_settings` (`id`,`listId`,`isEnabled`,`frequency`,`hour`,`minute`,`weekDaysMask`,`oneTimeEpochMillis`,`intervalValue`,`intervalUnit`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: NotificationSettingEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.listId)
        val _tmp: Int = if (entity.isEnabled) 1 else 0
        statement.bindLong(3, _tmp.toLong())
        statement.bindText(4, entity.frequency)
        statement.bindLong(5, entity.hour.toLong())
        statement.bindLong(6, entity.minute.toLong())
        statement.bindLong(7, entity.weekDaysMask.toLong())
        statement.bindLong(8, entity.oneTimeEpochMillis)
        statement.bindLong(9, entity.intervalValue.toLong())
        statement.bindText(10, entity.intervalUnit)
      }
    }
    this.__deleteAdapterOfNotificationSettingEntity = object : EntityDeleteOrUpdateAdapter<NotificationSettingEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `notification_settings` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: NotificationSettingEntity) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun upsertSetting(setting: NotificationSettingEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfNotificationSettingEntity.insertAndReturnId(_connection, setting)
    _result
  }

  public override suspend fun deleteSetting(setting: NotificationSettingEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfNotificationSettingEntity.handle(_connection, setting)
  }

  public override fun getSettingForList(listId: Long): Flow<NotificationSettingEntity?> {
    val _sql: String = "SELECT * FROM notification_settings WHERE listId = ?"
    return createFlow(__db, false, arrayOf("notification_settings")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, listId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfListId: Int = getColumnIndexOrThrow(_stmt, "listId")
        val _columnIndexOfIsEnabled: Int = getColumnIndexOrThrow(_stmt, "isEnabled")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfHour: Int = getColumnIndexOrThrow(_stmt, "hour")
        val _columnIndexOfMinute: Int = getColumnIndexOrThrow(_stmt, "minute")
        val _columnIndexOfWeekDaysMask: Int = getColumnIndexOrThrow(_stmt, "weekDaysMask")
        val _columnIndexOfOneTimeEpochMillis: Int = getColumnIndexOrThrow(_stmt, "oneTimeEpochMillis")
        val _columnIndexOfIntervalValue: Int = getColumnIndexOrThrow(_stmt, "intervalValue")
        val _columnIndexOfIntervalUnit: Int = getColumnIndexOrThrow(_stmt, "intervalUnit")
        val _result: NotificationSettingEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpListId: Long
          _tmpListId = _stmt.getLong(_columnIndexOfListId)
          val _tmpIsEnabled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsEnabled).toInt()
          _tmpIsEnabled = _tmp != 0
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpHour: Int
          _tmpHour = _stmt.getLong(_columnIndexOfHour).toInt()
          val _tmpMinute: Int
          _tmpMinute = _stmt.getLong(_columnIndexOfMinute).toInt()
          val _tmpWeekDaysMask: Int
          _tmpWeekDaysMask = _stmt.getLong(_columnIndexOfWeekDaysMask).toInt()
          val _tmpOneTimeEpochMillis: Long
          _tmpOneTimeEpochMillis = _stmt.getLong(_columnIndexOfOneTimeEpochMillis)
          val _tmpIntervalValue: Int
          _tmpIntervalValue = _stmt.getLong(_columnIndexOfIntervalValue).toInt()
          val _tmpIntervalUnit: String
          _tmpIntervalUnit = _stmt.getText(_columnIndexOfIntervalUnit)
          _result = NotificationSettingEntity(_tmpId,_tmpListId,_tmpIsEnabled,_tmpFrequency,_tmpHour,_tmpMinute,_tmpWeekDaysMask,_tmpOneTimeEpochMillis,_tmpIntervalValue,_tmpIntervalUnit)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllEnabledSettings(): List<NotificationSettingEntity> {
    val _sql: String = "SELECT * FROM notification_settings WHERE isEnabled = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfListId: Int = getColumnIndexOrThrow(_stmt, "listId")
        val _columnIndexOfIsEnabled: Int = getColumnIndexOrThrow(_stmt, "isEnabled")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfHour: Int = getColumnIndexOrThrow(_stmt, "hour")
        val _columnIndexOfMinute: Int = getColumnIndexOrThrow(_stmt, "minute")
        val _columnIndexOfWeekDaysMask: Int = getColumnIndexOrThrow(_stmt, "weekDaysMask")
        val _columnIndexOfOneTimeEpochMillis: Int = getColumnIndexOrThrow(_stmt, "oneTimeEpochMillis")
        val _columnIndexOfIntervalValue: Int = getColumnIndexOrThrow(_stmt, "intervalValue")
        val _columnIndexOfIntervalUnit: Int = getColumnIndexOrThrow(_stmt, "intervalUnit")
        val _result: MutableList<NotificationSettingEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: NotificationSettingEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpListId: Long
          _tmpListId = _stmt.getLong(_columnIndexOfListId)
          val _tmpIsEnabled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsEnabled).toInt()
          _tmpIsEnabled = _tmp != 0
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpHour: Int
          _tmpHour = _stmt.getLong(_columnIndexOfHour).toInt()
          val _tmpMinute: Int
          _tmpMinute = _stmt.getLong(_columnIndexOfMinute).toInt()
          val _tmpWeekDaysMask: Int
          _tmpWeekDaysMask = _stmt.getLong(_columnIndexOfWeekDaysMask).toInt()
          val _tmpOneTimeEpochMillis: Long
          _tmpOneTimeEpochMillis = _stmt.getLong(_columnIndexOfOneTimeEpochMillis)
          val _tmpIntervalValue: Int
          _tmpIntervalValue = _stmt.getLong(_columnIndexOfIntervalValue).toInt()
          val _tmpIntervalUnit: String
          _tmpIntervalUnit = _stmt.getText(_columnIndexOfIntervalUnit)
          _item = NotificationSettingEntity(_tmpId,_tmpListId,_tmpIsEnabled,_tmpFrequency,_tmpHour,_tmpMinute,_tmpWeekDaysMask,_tmpOneTimeEpochMillis,_tmpIntervalValue,_tmpIntervalUnit)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllEnabledSettingsFlow(): Flow<List<NotificationSettingEntity>> {
    val _sql: String = "SELECT * FROM notification_settings WHERE isEnabled = 1"
    return createFlow(__db, false, arrayOf("notification_settings")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfListId: Int = getColumnIndexOrThrow(_stmt, "listId")
        val _columnIndexOfIsEnabled: Int = getColumnIndexOrThrow(_stmt, "isEnabled")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfHour: Int = getColumnIndexOrThrow(_stmt, "hour")
        val _columnIndexOfMinute: Int = getColumnIndexOrThrow(_stmt, "minute")
        val _columnIndexOfWeekDaysMask: Int = getColumnIndexOrThrow(_stmt, "weekDaysMask")
        val _columnIndexOfOneTimeEpochMillis: Int = getColumnIndexOrThrow(_stmt, "oneTimeEpochMillis")
        val _columnIndexOfIntervalValue: Int = getColumnIndexOrThrow(_stmt, "intervalValue")
        val _columnIndexOfIntervalUnit: Int = getColumnIndexOrThrow(_stmt, "intervalUnit")
        val _result: MutableList<NotificationSettingEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: NotificationSettingEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpListId: Long
          _tmpListId = _stmt.getLong(_columnIndexOfListId)
          val _tmpIsEnabled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsEnabled).toInt()
          _tmpIsEnabled = _tmp != 0
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpHour: Int
          _tmpHour = _stmt.getLong(_columnIndexOfHour).toInt()
          val _tmpMinute: Int
          _tmpMinute = _stmt.getLong(_columnIndexOfMinute).toInt()
          val _tmpWeekDaysMask: Int
          _tmpWeekDaysMask = _stmt.getLong(_columnIndexOfWeekDaysMask).toInt()
          val _tmpOneTimeEpochMillis: Long
          _tmpOneTimeEpochMillis = _stmt.getLong(_columnIndexOfOneTimeEpochMillis)
          val _tmpIntervalValue: Int
          _tmpIntervalValue = _stmt.getLong(_columnIndexOfIntervalValue).toInt()
          val _tmpIntervalUnit: String
          _tmpIntervalUnit = _stmt.getText(_columnIndexOfIntervalUnit)
          _item = NotificationSettingEntity(_tmpId,_tmpListId,_tmpIsEnabled,_tmpFrequency,_tmpHour,_tmpMinute,_tmpWeekDaysMask,_tmpOneTimeEpochMillis,_tmpIntervalValue,_tmpIntervalUnit)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
