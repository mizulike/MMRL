package com.dergoogler.mmrl.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dergoogler.mmrl.ui.screens.home.screens.AboutScreen
import com.dergoogler.mmrl.ui.screens.home.screens.ThankYouScreen
import com.dergoogler.mmrl.ui.screens.main.BottomBarMainScreen

enum class MainRoute(val route: String) {
    BottomNavScreen("BottomNavScreen"),
    About("About"),
    ThankYou("ThankYou")
}

fun NavGraphBuilder.mainScreen() {
    composable(
        route = MainRoute.BottomNavScreen.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        BottomBarMainScreen()
    }

    composable(
        route = MainRoute.About.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        AboutScreen()
    }

    composable(
        route = MainRoute.ThankYou.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        ThankYouScreen()
    }
}