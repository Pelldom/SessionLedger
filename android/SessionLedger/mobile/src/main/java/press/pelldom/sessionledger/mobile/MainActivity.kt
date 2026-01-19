package press.pelldom.sessionledger.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import press.pelldom.sessionledger.mobile.ui.active.ActiveSessionScreen
import press.pelldom.sessionledger.mobile.ui.categories.CategoryDetailScreen
import press.pelldom.sessionledger.mobile.ui.categories.CategoryManagementScreen
import press.pelldom.sessionledger.mobile.ui.navigation.MobileRoutes
import press.pelldom.sessionledger.mobile.ui.detail.SessionDetailScreen
import press.pelldom.sessionledger.mobile.ui.detail.SessionBillingOverrideScreen
import press.pelldom.sessionledger.mobile.ui.detail.SessionTimingEditScreen
import press.pelldom.sessionledger.mobile.ui.export.ExportScreen
import press.pelldom.sessionledger.mobile.ui.sessions.SessionListScreen
import press.pelldom.sessionledger.mobile.ui.settings.AppSettingsScreen
import press.pelldom.sessionledger.mobile.ui.settings.SettingsScreen
import press.pelldom.sessionledger.mobile.wear.WearCategoriesPublisher
import press.pelldom.sessionledger.mobile.ui.AppVersion
import press.pelldom.sessionledger.mobile.appsettings.AppSettings
import press.pelldom.sessionledger.mobile.appsettings.AppSettingsRepository
import press.pelldom.sessionledger.mobile.appsettings.ThemeMode
import press.pelldom.sessionledger.mobile.settings.dataStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val repo = remember { AppSettingsRepository(application.dataStore) }
            val settings by repo.settings.collectAsState(initial = AppSettings())

            val dark = when {
                settings.themeMode == ThemeMode.DARK -> true
                settings.themeMode == ThemeMode.LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                MobileApp()
            }
        }
    }
}

@Composable
private fun MobileApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val sessionsFilterResetNonce = androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableIntStateOf(0)
    }

    val bottomItems = remember {
        listOf(
            BottomNavItem(
                route = MobileRoutes.ACTIVE,
                label = "Active",
                icon = Icons.Filled.PlayArrow
            ),
            BottomNavItem(
                route = MobileRoutes.SESSIONS,
                label = "Sessions",
                icon = Icons.AutoMirrored.Filled.List
            ),
            BottomNavItem(
                route = MobileRoutes.CATEGORIES,
                label = "Categories",
                icon = Icons.AutoMirrored.Filled.List
            )
        )
    }

    val showBottomBar = currentDestination
        ?.hierarchy
        ?.any { it.route in setOf(MobileRoutes.ACTIVE, MobileRoutes.SESSIONS, MobileRoutes.CATEGORIES) } == true

    LaunchedEffect(Unit) {
        // Start publishing categories to watch at app start.
        WearCategoriesPublisher.start(context)
    }

    Scaffold(
        bottomBar = {
            if (!showBottomBar) return@Scaffold

            Column {
                Text(
                    text = AppVersion.FOOTER_TEXT,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                NavigationBar {
                    bottomItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MobileRoutes.ACTIVE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MobileRoutes.ACTIVE) { ActiveSessionScreen() }
            composable(MobileRoutes.SESSIONS) {
                SessionListScreen(
                    onSessionClick = { id -> navController.navigate(MobileRoutes.sessionDetailRoute(id)) },
                    onExportClick = { navController.navigate(MobileRoutes.EXPORT) },
                    resetToActiveNonce = sessionsFilterResetNonce.intValue
                )
            }
            composable(MobileRoutes.CATEGORIES) {
                CategoryManagementScreen(
                    onOpenSettings = { navController.navigate(MobileRoutes.SETTINGS) },
                    onOpenAppSettings = { navController.navigate(MobileRoutes.APP_SETTINGS) },
                    onOpenCategory = { id -> navController.navigate(MobileRoutes.categoryDetailRoute(id)) }
                )
            }
            composable(MobileRoutes.SETTINGS) { SettingsScreen(onBack = { navController.popBackStack() }) }
            composable(MobileRoutes.APP_SETTINGS) { AppSettingsScreen(onBack = { navController.popBackStack() }) }
            composable(MobileRoutes.EXPORT) {
                ExportScreen(
                    onDone = { navController.popBackStack() },
                    onArchivedDone = {
                        sessionsFilterResetNonce.intValue++
                        navController.popBackStack(MobileRoutes.SESSIONS, false)
                    }
                )
            }

            composable(
                route = MobileRoutes.CATEGORY_DETAIL_ROUTE,
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
                CategoryDetailScreen(
                    categoryId = categoryId,
                    onDone = { navController.popBackStack(MobileRoutes.CATEGORIES, false) }
                )
            }

            composable(
                route = MobileRoutes.SESSION_DETAIL_ROUTE,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
                SessionDetailScreen(
                    sessionId = sessionId,
                    onDone = { navController.popBackStack(MobileRoutes.SESSIONS, false) },
                    onOpenTimingEdit = { navController.navigate(MobileRoutes.sessionTimingEditRoute(sessionId)) },
                    onOpenBillingOverrides = { navController.navigate(MobileRoutes.sessionBillingOverrideRoute(sessionId)) }
                )
            }

            composable(
                route = MobileRoutes.SESSION_TIMING_EDIT_ROUTE,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
                SessionTimingEditScreen(
                    sessionId = sessionId,
                    onDone = { navController.popBackStack() }
                )
            }

            composable(
                route = MobileRoutes.SESSION_BILLING_OVERRIDE_ROUTE,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
                SessionBillingOverrideScreen(
                    sessionId = sessionId,
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
