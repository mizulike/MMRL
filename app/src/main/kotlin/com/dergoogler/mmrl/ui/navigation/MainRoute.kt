package com.dergoogler.mmrl.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dergoogler.mmrl.ui.screens.home.screens.AboutScreen
import com.dergoogler.mmrl.ui.screens.home.screens.ThankYouScreen
import com.dergoogler.mmrl.ui.screens.main.BottomBarMainScreen
import com.dergoogler.mmrl.ui.screens.search.SearchScreen
import com.dergoogler.mmrl.viewmodel.SearchViewModel

enum class MainRoute(val route: String) {
    BottomNavScreen("BottomNavScreen"),
    About("About"),
    ThankYou("ThankYou"),
    GlobalSearch("GlobalSearch")
}

fun NavGraphBuilder.mainScreen() {
    composable(
        route = MainRoute.GlobalSearch.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        val viewModel = hiltViewModel<SearchViewModel>()

        SearchScreen(viewModel)
    }

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