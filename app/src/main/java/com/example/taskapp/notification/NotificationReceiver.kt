package com.example.taskapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.taskapp.data.local.TaskAppDatabase
import com.example.taskapp.data.local.toDomain
import com.example.taskapp.domain.model.NotificationFrequency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_LIST_ID = "extra_list_id"
        const val EXTRA_DAY_INDEX = "extra_day_index"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> rescheduleAllAlarms(context)
            else -> handleNotification(context, intent)
        }
    }

    private fun handleNotification(context: Context, intent: Intent) {
        val listId = intent.getLongExtra(EXTRA_LIST_ID, -1L)
        if (listId == -1L) return

        // goAsync() grants up to 10 seconds for the coroutine to complete before
        // the system considers the receiver done — needed for the Room queries below.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = TaskAppDatabase.getInstance(context)
                val listEntity = db.taskListDao().getListById(listId).first() ?: return@launch
                val items = db.taskItemDao().getItemsForList(listId).first()
                val settingEntity = db.notificationSettingDao().getSettingForList(listId).first()
                    ?: return@launch
                if (!settingEntity.isEnabled) return@launch

                val list = listEntity.toDomain()
                val setting = settingEntity.toDomain()
                NotificationHelper.showNotification(context, list, items.map { it.toDomain() })

                val scheduler = AlarmScheduler(context)
                when (setting.frequency) {
                    NotificationFrequency.DAILY ->
                        scheduler.schedule(setting)
                    NotificationFrequency.WEEKLY -> {
                        val dayIndex = intent.getIntExtra(EXTRA_DAY_INDEX, -1)
                        if (dayIndex >= 0) {
                            scheduler.schedule(setting.copy(weekDaysMask = 1 shl dayIndex))
                        }
                    }
                    NotificationFrequency.CUSTOM_INTERVAL ->
                        scheduler.scheduleNextCustomInterval(
                            setting.listId, setting.intervalValue, setting.intervalUnit
                        )
                    NotificationFrequency.ONE_TIME -> { /* no reschedule */ }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun rescheduleAllAlarms(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = TaskAppDatabase.getInstance(context)
                val scheduler = AlarmScheduler(context)
                db.notificationSettingDao().getAllEnabledSettings().forEach { entity ->
                    scheduler.schedule(entity.toDomain())
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
