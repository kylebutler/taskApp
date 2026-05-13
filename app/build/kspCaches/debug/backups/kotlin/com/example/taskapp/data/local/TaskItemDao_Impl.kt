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
public class TaskItemDao_Impl(
  __db: RoomDatabase,
) : TaskItemDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTaskItemEntity: EntityInsertAdapter<TaskItemEntity>

  private val __deleteAdapterOfTaskItemEntity: EntityDeleteOrUpdateAdapter<TaskItemEntity>

  private val __updateAdapterOfTaskItemEntity: EntityDeleteOrUpdateAdapter<TaskItemEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTaskItemEntity = object : EntityInsertAdapter<TaskItemEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `task_items` (`id`,`listId`,`text`,`isChecked`,`position`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TaskItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.listId)
        statement.bindText(3, entity.text)
        val _tmp: Int = if (entity.isChecked) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        statement.bindLong(5, entity.position.toLong())
      }
    }
    this.__deleteAdapterOfTaskItemEntity = object : EntityDeleteOrUpdateAdapter<TaskItemEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `task_items` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TaskItemEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfTaskItemEntity = object : EntityDeleteOrUpdateAdapter<TaskItemEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `task_items` SET `id` = ?,`listId` = ?,`text` = ?,`isChecked` = ?,`position` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TaskItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.listId)
        statement.bindText(3, entity.text)
        val _tmp: Int = if (entity.isChecked) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        statement.bindLong(5, entity.position.toLong())
        statement.bindLong(6, entity.id)
      }
    }
  }

  public override suspend fun insertItem(item: TaskItemEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfTaskItemEntity.insertAndReturnId(_connection, item)
    _result
  }

  public override suspend fun deleteItem(item: TaskItemEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfTaskItemEntity.handle(_connection, item)
  }

  public override suspend fun updateItem(item: TaskItemEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTaskItemEntity.handle(_connection, item)
  }

  public override suspend fun updateItems(items: List<TaskItemEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTaskItemEntity.handleMultiple(_connection, items)
  }

  public override fun getItemsForList(listId: Long): Flow<List<TaskItemEntity>> {
    val _sql: String = "SELECT * FROM task_items WHERE listId = ? ORDER BY position ASC"
    return createFlow(__db, false, arrayOf("task_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, listId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfListId: Int = getColumnIndexOrThrow(_stmt, "listId")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfIsChecked: Int = getColumnIndexOrThrow(_stmt, "isChecked")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _result: MutableList<TaskItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaskItemEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpListId: Long
          _tmpListId = _stmt.getLong(_columnIndexOfListId)
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpIsChecked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsChecked).toInt()
          _tmpIsChecked = _tmp != 0
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          _item = TaskItemEntity(_tmpId,_tmpListId,_tmpText,_tmpIsChecked,_tmpPosition)
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
