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
public class TaskListDao_Impl(
  __db: RoomDatabase,
) : TaskListDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTaskListEntity: EntityInsertAdapter<TaskListEntity>

  private val __deleteAdapterOfTaskListEntity: EntityDeleteOrUpdateAdapter<TaskListEntity>

  private val __updateAdapterOfTaskListEntity: EntityDeleteOrUpdateAdapter<TaskListEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTaskListEntity = object : EntityInsertAdapter<TaskListEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `task_lists` (`id`,`title`,`type`,`textContent`,`colorArgb`,`isDeleted`,`isArchived`,`deletedAt`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TaskListEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.type)
        val _tmpTextContent: String? = entity.textContent
        if (_tmpTextContent == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpTextContent)
        }
        val _tmpColorArgb: Int? = entity.colorArgb
        if (_tmpColorArgb == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpColorArgb.toLong())
        }
        val _tmp: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmp_1: Int = if (entity.isArchived) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        val _tmpDeletedAt: Long? = entity.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpDeletedAt)
        }
        statement.bindLong(9, entity.createdAt)
      }
    }
    this.__deleteAdapterOfTaskListEntity = object : EntityDeleteOrUpdateAdapter<TaskListEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `task_lists` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TaskListEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfTaskListEntity = object : EntityDeleteOrUpdateAdapter<TaskListEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `task_lists` SET `id` = ?,`title` = ?,`type` = ?,`textContent` = ?,`colorArgb` = ?,`isDeleted` = ?,`isArchived` = ?,`deletedAt` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TaskListEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.type)
        val _tmpTextContent: String? = entity.textContent
        if (_tmpTextContent == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpTextContent)
        }
        val _tmpColorArgb: Int? = entity.colorArgb
        if (_tmpColorArgb == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpColorArgb.toLong())
        }
        val _tmp: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmp_1: Int = if (entity.isArchived) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        val _tmpDeletedAt: Long? = entity.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpDeletedAt)
        }
        statement.bindLong(9, entity.createdAt)
        statement.bindLong(10, entity.id)
      }
    }
  }

  public override suspend fun insertList(list: TaskListEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfTaskListEntity.insertAndReturnId(_connection, list)
    _result
  }

  public override suspend fun deleteList(list: TaskListEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfTaskListEntity.handle(_connection, list)
  }

  public override suspend fun updateList(list: TaskListEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTaskListEntity.handle(_connection, list)
  }

  public override fun getAllLists(): Flow<List<TaskListEntity>> {
    val _sql: String = "SELECT * FROM task_lists WHERE isDeleted = 0 AND isArchived = 0 ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("task_lists")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTextContent: Int = getColumnIndexOrThrow(_stmt, "textContent")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfIsArchived: Int = getColumnIndexOrThrow(_stmt, "isArchived")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<TaskListEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaskListEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTextContent: String?
          if (_stmt.isNull(_columnIndexOfTextContent)) {
            _tmpTextContent = null
          } else {
            _tmpTextContent = _stmt.getText(_columnIndexOfTextContent)
          }
          val _tmpColorArgb: Int?
          if (_stmt.isNull(_columnIndexOfColorArgb)) {
            _tmpColorArgb = null
          } else {
            _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb).toInt()
          }
          val _tmpIsDeleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp != 0
          val _tmpIsArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsArchived).toInt()
          _tmpIsArchived = _tmp_1 != 0
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = TaskListEntity(_tmpId,_tmpTitle,_tmpType,_tmpTextContent,_tmpColorArgb,_tmpIsDeleted,_tmpIsArchived,_tmpDeletedAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTrashLists(): Flow<List<TaskListEntity>> {
    val _sql: String = "SELECT * FROM task_lists WHERE isDeleted = 1 ORDER BY deletedAt DESC"
    return createFlow(__db, false, arrayOf("task_lists")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTextContent: Int = getColumnIndexOrThrow(_stmt, "textContent")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfIsArchived: Int = getColumnIndexOrThrow(_stmt, "isArchived")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<TaskListEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaskListEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTextContent: String?
          if (_stmt.isNull(_columnIndexOfTextContent)) {
            _tmpTextContent = null
          } else {
            _tmpTextContent = _stmt.getText(_columnIndexOfTextContent)
          }
          val _tmpColorArgb: Int?
          if (_stmt.isNull(_columnIndexOfColorArgb)) {
            _tmpColorArgb = null
          } else {
            _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb).toInt()
          }
          val _tmpIsDeleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp != 0
          val _tmpIsArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsArchived).toInt()
          _tmpIsArchived = _tmp_1 != 0
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = TaskListEntity(_tmpId,_tmpTitle,_tmpType,_tmpTextContent,_tmpColorArgb,_tmpIsDeleted,_tmpIsArchived,_tmpDeletedAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getArchivedLists(): Flow<List<TaskListEntity>> {
    val _sql: String = "SELECT * FROM task_lists WHERE isArchived = 1 AND isDeleted = 0 ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("task_lists")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTextContent: Int = getColumnIndexOrThrow(_stmt, "textContent")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfIsArchived: Int = getColumnIndexOrThrow(_stmt, "isArchived")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<TaskListEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaskListEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTextContent: String?
          if (_stmt.isNull(_columnIndexOfTextContent)) {
            _tmpTextContent = null
          } else {
            _tmpTextContent = _stmt.getText(_columnIndexOfTextContent)
          }
          val _tmpColorArgb: Int?
          if (_stmt.isNull(_columnIndexOfColorArgb)) {
            _tmpColorArgb = null
          } else {
            _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb).toInt()
          }
          val _tmpIsDeleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp != 0
          val _tmpIsArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsArchived).toInt()
          _tmpIsArchived = _tmp_1 != 0
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = TaskListEntity(_tmpId,_tmpTitle,_tmpType,_tmpTextContent,_tmpColorArgb,_tmpIsDeleted,_tmpIsArchived,_tmpDeletedAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getListById(listId: Long): Flow<TaskListEntity?> {
    val _sql: String = "SELECT * FROM task_lists WHERE id = ?"
    return createFlow(__db, false, arrayOf("task_lists")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, listId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTextContent: Int = getColumnIndexOrThrow(_stmt, "textContent")
        val _columnIndexOfColorArgb: Int = getColumnIndexOrThrow(_stmt, "colorArgb")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfIsArchived: Int = getColumnIndexOrThrow(_stmt, "isArchived")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: TaskListEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTextContent: String?
          if (_stmt.isNull(_columnIndexOfTextContent)) {
            _tmpTextContent = null
          } else {
            _tmpTextContent = _stmt.getText(_columnIndexOfTextContent)
          }
          val _tmpColorArgb: Int?
          if (_stmt.isNull(_columnIndexOfColorArgb)) {
            _tmpColorArgb = null
          } else {
            _tmpColorArgb = _stmt.getLong(_columnIndexOfColorArgb).toInt()
          }
          val _tmpIsDeleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp != 0
          val _tmpIsArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsArchived).toInt()
          _tmpIsArchived = _tmp_1 != 0
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = TaskListEntity(_tmpId,_tmpTitle,_tmpType,_tmpTextContent,_tmpColorArgb,_tmpIsDeleted,_tmpIsArchived,_tmpDeletedAt,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteOldTrash(timestamp: Long) {
    val _sql: String = "DELETE FROM task_lists WHERE isDeleted = 1 AND deletedAt < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, timestamp)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
