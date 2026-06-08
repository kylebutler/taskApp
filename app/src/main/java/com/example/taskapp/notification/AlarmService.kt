package com.example.taskapp.notification

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.taskapp.TaskApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmService : Service() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra("ALARM_ID", -1L) ?: -1L
        val label = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val isSilent = intent?.getBooleanExtra("IS_SILENT", false) ?: false

        if (alarmId != -1L) {
            startAlarm(alarmId, label, isSilent)
        }

        return START_NOT_STICKY
    }

    private fun startAlarm(alarmId: Long, label: String, isSilent: Boolean) {
        val app = applicationContext as TaskApp
        
        CoroutineScope(Dispatchers.Main).launch {
            val prefs = app.userPreferencesRepository
            val ringtoneUriString = prefs.alarmRingtoneUri.first()
            
            // 1. Handle Sound
            if (!isSilent && ringtoneUriString != null) {
                val uri = Uri.parse(ringtoneUriString)
                ringtone = RingtoneManager.getRingtone(this@AlarmService, uri)?.apply {
                    audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    play()
                }
            }

            // 2. Handle Vibration (Vibrate by default for all alarms)
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 500, 500, 500)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))

            // 3. Start Foreground
            val builder = NotificationHelper.buildClockAlarmNotification(this@AlarmService, alarmId, label)
            val notification = builder.build()
            notification.flags = notification.flags or NotificationCompat.FLAG_INSISTENT

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                startForeground(
                    alarmId.toInt(), 
                    notification, 
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(alarmId.toInt(), notification)
            }
        }
    }

    override fun onDestroy() {
        ringtone?.stop()
        vibrator?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
