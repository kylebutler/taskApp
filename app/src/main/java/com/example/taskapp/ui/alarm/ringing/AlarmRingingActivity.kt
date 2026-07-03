package com.example.taskapp.ui.alarm.ringing

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskapp.notification.ClockAlarmReceiver
import com.example.taskapp.ui.theme.TaskAppTheme

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val alarmId = intent.getLongExtra("ALARM_ID", -1L)
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"

        setContent {
            TaskAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(bottom = 64.dp)
                        )
                        
                        Button(
                            onClick = {
                                sendBroadcastAction("STOP_ALARM", alarmId)
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth(0.7f).height(64.dp)
                        ) {
                            Text("Stop")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = {
                                sendBroadcastAction("SNOOZE_ALARM", alarmId)
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth(0.7f).height(64.dp)
                        ) {
                            Text("Snooze")
                        }
                    }
                }
            }
        }
    }

    private fun sendBroadcastAction(action: String, alarmId: Long) {
        val intent = Intent(this, ClockAlarmReceiver::class.java).apply {
            this.action = action
            putExtra("ALARM_ID", alarmId)
        }
        sendBroadcast(intent)
    }
}
