package com.antigravity.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.antigravity.mobile.presentation.auth.LoginScreen
import com.antigravity.mobile.presentation.main.MainScreen
import com.antigravity.mobile.presentation.history.TradeHistoryScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object TradeHistory : Screen("trade_history")
}

@Composable
fun RootNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Main.route) {
            MainScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.TradeHistory.route)
                }
            )
        }

        composable(Screen.TradeHistory.route) {
            TradeHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
