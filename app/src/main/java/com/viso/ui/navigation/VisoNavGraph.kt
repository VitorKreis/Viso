package com.viso.ui.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.viso.ui.agenda.AgendaScreen
import com.viso.ui.bills.BillsScreen
import com.viso.ui.config.ConfigScreen
import com.viso.ui.goals.GoalsScreen
import com.viso.ui.home.HomeScreen
import com.viso.ui.onboarding.OnboardingScreen
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.BgCard2
import com.viso.ui.theme.TextMuted

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Início", Icons.Rounded.Home),
    BottomNavItem(Screen.Bills, "Contas", Icons.Rounded.Receipt),
    BottomNavItem(Screen.Goals, "Metas", Icons.Rounded.Savings),
    BottomNavItem(Screen.Agenda, "Agenda", Icons.Rounded.CalendarMonth)
)

@Composable
fun VisoNavGraph(startDestination: String) {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    rootNavController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            MainScaffold(rootNavController, initialTab = Screen.Home)
        }
        composable(Screen.Bills.route) {
            MainScaffold(rootNavController, initialTab = Screen.Bills)
        }
        composable(Screen.Goals.route) {
            MainScaffold(rootNavController, initialTab = Screen.Goals)
        }
        composable(Screen.Agenda.route) {
            MainScaffold(rootNavController, initialTab = Screen.Agenda)
        }
        composable(Screen.Config.route) {
            ConfigScreen(onBack = { rootNavController.popBackStack() })
        }
    }
}

@Composable
fun MainScaffold(rootNavController: NavHostController, initialTab: Screen) {
    val tabNavController = rememberNavController()

    Scaffold(
        containerColor = com.viso.ui.theme.BgApp,
        bottomBar = {
            NavigationBar(
                containerColor = BgCard2,
                tonalElevation = androidx.compose.ui.unit.Dp(0f),
                modifier = Modifier.navigationBarsPadding()
            ) {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            tabNavController.navigate(item.screen.route) {
                                popUpTo(tabNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(item.icon, contentDescription = item.label)
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = AccentBlue.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = initialTab.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onNavigateToConfig = {
                    rootNavController.navigate(Screen.Config.route)
                })
            }
            composable(Screen.Bills.route) {
                BillsScreen()
            }
            composable(Screen.Goals.route) {
                GoalsScreen()
            }
            composable(Screen.Agenda.route) {
                AgendaScreen()
            }
        }
    }
}
