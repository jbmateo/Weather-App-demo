package com.demo.weatherapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.demo.weatherapp.ui.screens.auth.AuthScreen
import com.demo.weatherapp.ui.screens.auth.AuthViewModel
import com.demo.weatherapp.ui.screens.main.MainScreen

private sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Main : AppRoute("main")
}

@Composable
fun WeatherApp(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(authState.isLoggedIn, currentRoute) {
        if (currentRoute == null) return@LaunchedEffect

        if (authState.isLoggedIn && currentRoute == AppRoute.Login.route) {
            val returnedToMain = navController.popBackStack(AppRoute.Main.route, inclusive = false)
            if (!returnedToMain) {
                navController.navigate(AppRoute.Main.route) {
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Main.route
    ) {
        composable(AppRoute.Login.route) {
            AuthScreen(
                state = authState,
                onNameChange = authViewModel::onNameChange,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onModeChange = authViewModel::setSignupMode,
                onSubmit = authViewModel::submit
            )
        }

        composable(AppRoute.Main.route) {
            MainScreen(
                isLoggedIn = authState.isLoggedIn,
                currentUserName = authState.currentUserName,
                onLoginClick = {
                    navController.navigate(AppRoute.Login.route) {
                        launchSingleTop = true
                    }
                },
                onLogout = authViewModel::logout
            )
        }
    }
}
