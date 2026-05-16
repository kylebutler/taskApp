package com.example.taskapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskapp.TaskApp
import com.example.taskapp.notification.AlarmScheduler
import com.example.taskapp.ui.archive.ArchiveScreen
import com.example.taskapp.ui.archive.ArchiveViewModel
import com.example.taskapp.ui.detail.ListDetailScreen
import com.example.taskapp.ui.detail.ListDetailViewModel
import com.example.taskapp.ui.home.HomeScreen
import com.example.taskapp.ui.home.HomeViewModel
import com.example.taskapp.ui.notifications.NotificationSettingsScreen
import com.example.taskapp.ui.notifications.NotificationSettingsViewModel
import com.example.taskapp.ui.settings.SettingsScreen
import com.example.taskapp.ui.trash.TrashScreen
import com.example.taskapp.ui.trash.TrashViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Trash : Screen("trash")
    data object Archive : Screen("archive")
    data object Settings : Screen("settings")
    data object ListDetail : Screen("list_detail/{listId}") {
        fun createRoute(listId: Long) = "list_detail/$listId"
    }
    data object NotificationSettings : Screen("notification_settings/{listId}") {
        fun createRoute(listId: Long) = "notification_settings/$listId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startListId: Long? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as? TaskApp ?: return

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel(factory = HomeViewModel.Factory(app.taskRepository)),
                onListClick = { listId -> navController.navigate(Screen.ListDetail.createRoute(listId)) },
                onTrashClick = { navController.navigate(Screen.Trash.route) },
                onArchiveClick = { navController.navigate(Screen.Archive.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Trash.route) {
            TrashScreen(
                viewModel = viewModel(factory = TrashViewModel.Factory(app.taskRepository)),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Archive.route) {
            ArchiveScreen(
                viewModel = viewModel(factory = ArchiveViewModel.Factory(app.taskRepository)),
                onBack = { navController.popBackStack() },
                onListClick = { listId -> navController.navigate(Screen.ListDetail.createRoute(listId)) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.ListDetail.route,
            arguments = listOf(navArgument("listId") { type = NavType.LongType })
        ) { backStack ->
            val listId = backStack.arguments!!.getLong("listId")
            ListDetailScreen(
                viewModel = viewModel(
                    factory = ListDetailViewModel.Factory(app, app.taskRepository, app.notificationRepository, listId)
                ),
                onBack = { navController.popBackStack() },
                onOpenNotificationSettings = {
                    navController.navigate(Screen.NotificationSettings.createRoute(listId))
                }
            )
        }

        composable(
            route = Screen.NotificationSettings.route,
            arguments = listOf(navArgument("listId") { type = NavType.LongType })
        ) { backStack ->
            val listId = backStack.arguments!!.getLong("listId")
            val ctx = LocalContext.current
            NotificationSettingsScreen(
                viewModel = viewModel(
                    factory = NotificationSettingsViewModel.Factory(
                        app.notificationRepository,
                        app.taskRepository,
                        AlarmScheduler(ctx),
                        listId
                    )
                ),
                onBack = { navController.popBackStack() }
            )
        }
    }

    LaunchedEffect(startListId) {
        if (startListId != null) {
            navController.navigate(Screen.ListDetail.createRoute(startListId))
        }
    }
}
