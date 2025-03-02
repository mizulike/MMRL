package com.dergoogler.mmrl.ui.navigation.graphs

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dergoogler.mmrl.ui.navigation.MainScreen
import com.dergoogler.mmrl.ui.screens.superuser.screens.superuser.SuperUserScreen
import com.dergoogler.mmrl.viewmodel.SuperUserViewModel

enum class SuperUserScreen(val route: String) {
    Home("SuperUser"),
}

fun NavGraphBuilder.superUserScreen() = navigation(
    startDestination = SuperUserScreen.Home.route,
    route = MainScreen.SuperUser.route
) {
    composable(
        route = SuperUserScreen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        val viewModel = hiltViewModel<SuperUserViewModel>()

        SuperUserScreen(viewModel = viewModel)
    }
//
//    composable<AppInfo> {
//        val app = it.toRoute<AppInfo>()
//        AppProfileScreen(app)
//    }
}