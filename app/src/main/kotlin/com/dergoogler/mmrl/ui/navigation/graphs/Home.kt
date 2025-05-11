package com.dergoogler.mmrl.ui.navigation.graphs

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dergoogler.mmrl.ui.navigation.BottomNavRoute
import com.dergoogler.mmrl.ui.screens.home.HomeScreen

enum class HomeScreen(val route: String) {
    Home("Home"),
}

fun NavGraphBuilder.homeScreen() = navigation(
    startDestination = HomeScreen.Home.route,
    route = BottomNavRoute.Home.route
) {
    composable(
        route = HomeScreen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        HomeScreen()
    }
}