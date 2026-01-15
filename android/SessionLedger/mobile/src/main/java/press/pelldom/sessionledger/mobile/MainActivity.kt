package press.pelldom.sessionledger.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import press.pelldom.sessionledger.mobile.ui.active.ActiveSessionScreen
import press.pelldom.sessionledger.mobile.ui.navigation.MobileRoutes
import press.pelldom.sessionledger.mobile.ui.sessions.SessionListScreen
import press.pelldom.sessionledger.mobile.ui.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MobileApp()
            }
        }
    }
}

@Composable
private fun MobileApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
                icon = Icons.Filled.List
            )
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(MobileRoutes.ACTIVE) { saveState = true }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
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
            composable(MobileRoutes.SESSIONS) { SessionListScreen() }
            composable(MobileRoutes.SETTINGS) { SettingsScreen() }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
