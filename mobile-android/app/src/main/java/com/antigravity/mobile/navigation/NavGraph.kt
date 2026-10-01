package com.antigravity.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.antigravity.mobile.presentation.auth.LoginScreen
import com.antigravity.mobile.presentation.main.MainScreen
import com.antigravity.mobile.presentation.history.TradeHistoryScreen
import com.antigravity.mobile.presentation.privacy.PrivacyPolicyScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.mobile.presentation.auth.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object TradeHistory : Screen("trade_history")
    object PrivacyPolicy : Screen("privacy_policy")
}

@Composable
fun RootNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onLogout = {
                    authViewModel?.logout()
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.TradeHistory.route)
                },
                onNavigateToPrivacy = {
                    navController.navigate(Screen.PrivacyPolicy.route)
                },
                onNavigateToLogin = {
                    authViewModel?.resetState()
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel ?: hiltViewModel(),
                onLoginSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TradeHistory.route) {
            TradeHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
