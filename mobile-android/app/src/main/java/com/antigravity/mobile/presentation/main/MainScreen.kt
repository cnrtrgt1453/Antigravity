package com.antigravity.mobile.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antigravity.mobile.presentation.home.HomeScreen
import com.antigravity.mobile.presentation.market.MarketScreen
import com.antigravity.mobile.presentation.news.NewsScreen
import com.antigravity.mobile.presentation.profile.ProfileScreen
import com.antigravity.mobile.presentation.signals.SignalsScreen

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreen("home", "Anasayfa", Icons.Default.Home)
    object Market : BottomBarScreen("market", "Piyasalar", Icons.Default.ShowChart)
    object Signals : BottomBarScreen("signals", "Sinyaller", Icons.Default.Analytics)
    object News : BottomBarScreen("news", "Haberler", Icons.Default.Newspaper)
    object Game : BottomBarScreen("game", "Portföy", Icons.Default.AccountBalanceWallet)
    object Profile : BottomBarScreen("profile", "Profil", Icons.Default.Person)
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomBarScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomBarScreen.Home.route) { 
                HomeScreen(onNavigateToLogin = onNavigateToLogin) 
            }
            composable(BottomBarScreen.Market.route) { 
                MarketScreen(onNavigateToLogin = onNavigateToLogin) 
            }
            composable(BottomBarScreen.Signals.route) { SignalsScreen() }
            composable(BottomBarScreen.News.route) { NewsScreen() }
            composable(BottomBarScreen.Game.route) {
                com.antigravity.mobile.presentation.game.GameScreen(
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToLogin = onNavigateToLogin
                )
            }
            composable(BottomBarScreen.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout,
                    onNavigateToPrivacy = onNavigateToPrivacy,
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Market,
        BottomBarScreen.Signals,
        BottomBarScreen.News,
        BottomBarScreen.Game,
        BottomBarScreen.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                label = {
                    Text(
                        text = screen.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                icon = { Icon(imageVector = screen.icon, contentDescription = "Navigation Icon") },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
