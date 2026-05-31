package com.example.taskapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskapp.data.repository.settings.ThemePreference
import com.example.taskapp.ui.navigation.AppNavigation
import com.example.taskapp.ui.theme.TaskAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = applicationContext as TaskApp
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
        enableEdgeToEdge()
        val openListId = intent.getLongExtra("open_list_id", -1L)
        setContent {
            val themePreference by app.userPreferencesRepository.themePreference.collectAsStateWithLifecycle(initialValue = ThemePreference.OS)
            
            TaskAppTheme(themePreference = themePreference) {
                AppNavigation(startListId = if (openListId != -1L) openListId else null)
            }
        }
    }
}
