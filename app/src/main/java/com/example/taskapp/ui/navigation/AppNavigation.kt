package com.example.taskapp.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.example.taskapp.ui.settings.SettingsViewModel
import com.example.taskapp.ui.settings.SettingsScreen
import com.example.taskapp.ui.trash.TrashScreen
import com.example.taskapp.ui.trash.TrashViewModel
import androidx.compose.material.icons.filled.Checklist
import com.example.taskapp.ui.tasks.MyTasksScreen
import com.example.taskapp.ui.tasks.MyTasksViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object MyTasks : Screen("my_tasks")
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute in listOf(Screen.Home.route, Screen.MyTasks.route, Screen.Archive.route, Screen.Trash.route, Screen.Settings.route),
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "TaskApp",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
                NavigationDrawerItem(
                    label = { Text("My Lists") },
                    selected = currentRoute == Screen.Home.route,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Menu, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("My Tasks") },
                    selected = currentRoute == Screen.MyTasks.route,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            if (currentRoute != Screen.MyTasks.route) {
                                navController.navigate(Screen.MyTasks.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Checklist, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Archive") },
                    selected = currentRoute == Screen.Archive.route,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            if (currentRoute != Screen.Archive.route) {
                                navController.navigate(Screen.Archive.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Archive, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Trash") },
                    selected = currentRoute == Screen.Trash.route,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            if (currentRoute != Screen.Trash.route) {
                                navController.navigate(Screen.Trash.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.DeleteForever, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = currentRoute == Screen.Settings.route,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            if (currentRoute != Screen.Settings.route) {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, null) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
                )
            }
        }
    ) {
        NavHost(navController = navController, startDestination = Screen.Home.route) {

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel(factory = HomeViewModel.Factory(app.taskRepository, AlarmScheduler(context))),
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onListClick = { listId -> navController.navigate(Screen.ListDetail.createRoute(listId)) }
                )
            }

            composable(Screen.MyTasks.route) {
                MyTasksScreen(
                    viewModel = viewModel(factory = MyTasksViewModel.Factory(app.taskRepository, AlarmScheduler(context))),
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onTaskClick = { taskId -> navController.navigate(Screen.NotificationSettings.createRoute(taskId)) }
                )
            }

            composable(Screen.Trash.route) {
                TrashScreen(
                    viewModel = viewModel(factory = TrashViewModel.Factory(app.taskRepository)),
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }

            composable(Screen.Archive.route) {
                ArchiveScreen(
                    viewModel = viewModel(factory = ArchiveViewModel.Factory(app.taskRepository)),
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onListClick = { listId -> navController.navigate(Screen.ListDetail.createRoute(listId)) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel(factory = SettingsViewModel.Factory(app.userPreferencesRepository)),
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }

            composable(
                route = Screen.ListDetail.route,
                arguments = listOf(navArgument("listId") { type = NavType.LongType })
            ) { backStack ->
                val listId = backStack.arguments!!.getLong("listId")
                ListDetailScreen(
                    viewModel = viewModel(
                        factory = ListDetailViewModel.Factory(app, app.taskRepository, app.notificationRepository, AlarmScheduler(context), listId)
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
    }

    LaunchedEffect(startListId) {
        if (startListId != null) {
            navController.navigate(Screen.ListDetail.createRoute(startListId))
        }
    }
}
