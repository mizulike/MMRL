package com.dergoogler.mmrl.ui.navigation.graphs

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dergoogler.mmrl.ui.navigation.BottomNavRoute
import com.dergoogler.mmrl.ui.screens.search.SearchScreen
import com.dergoogler.mmrl.viewmodel.SearchViewModel

enum class SearchScreen(val route: String) {
    Search("Search")
}

fun NavGraphBuilder.searchScreen() = navigation(
    startDestination = SearchScreen.Search.route,
    route = BottomNavRoute.Search.route
) {
    composable(
        route = SearchScreen.Search.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        val viewModel = hiltViewModel<SearchViewModel>()

        SearchScreen(viewModel)
    }
}