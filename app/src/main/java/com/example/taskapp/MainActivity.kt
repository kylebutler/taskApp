package com.example.taskapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.taskapp.ui.navigation.AppNavigation
import com.example.taskapp.ui.theme.TaskAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
        enableEdgeToEdge()
        val openListId = intent.getLongExtra("open_list_id", -1L)
        setContent {
            TaskAppTheme {
                AppNavigation(startListId = if (openListId != -1L) openListId else null)
            }
        }
    }
}
