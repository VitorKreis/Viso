package com.viso.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Bills : Screen("bills")
    object Goals : Screen("goals")
    object Agenda : Screen("agenda")
    object Config : Screen("config")
}
