package com.dergoogler.mmrl.ui.screens.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ui.navigation.MainRoute
import com.dergoogler.mmrl.ui.navigation.mainScreen
import com.dergoogler.mmrl.ui.providable.LocalMainNavController
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost

@Composable
fun MainScreen() {
    val mainNavController = LocalMainNavController.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.none,
    ) { paddingValues ->
        CompositionLocalProvider(
            LocalSnackbarHost provides snackbarHostState,
        ) {
            NavHost(
                modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
                navController = mainNavController,
                startDestination = MainRoute.BottomNavScreen.route
            ) {
                mainScreen()
            }
        }
    }
}
