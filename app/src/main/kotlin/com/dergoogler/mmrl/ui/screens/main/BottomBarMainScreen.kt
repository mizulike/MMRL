package com.dergoogler.mmrl.ui.screens.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dergoogler.mmrl.datastore.model.Homepage
import com.dergoogler.mmrl.datastore.model.WorkingMode.Companion.isRoot
import com.dergoogler.mmrl.ext.navigatePopUpTo
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.ui.navigation.BottomNavRoute
import com.dergoogler.mmrl.ui.navigation.graphs.homeScreen
import com.dergoogler.mmrl.ui.navigation.graphs.modulesScreen
import com.dergoogler.mmrl.ui.navigation.graphs.repositoryScreen
import com.dergoogler.mmrl.ui.navigation.graphs.settingsScreen
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.viewmodel.BulkInstallViewModel

@Composable
fun BottomBarMainScreen() {
    val userPreferences = LocalUserPreferences.current
    val bulkInstallViewModel: BulkInstallViewModel = hiltViewModel()

    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isRoot = userPreferences.workingMode.isRoot && Platform.isAlive

    val mainScreens by remember(isRoot) {
        derivedStateOf {
            if (isRoot) {
                return@derivedStateOf listOf(
                    BottomNavRoute.Home,
                    BottomNavRoute.Repository,
                    BottomNavRoute.Modules,
                    BottomNavRoute.Settings,
                )
            }

            return@derivedStateOf listOf(
                BottomNavRoute.Home,
                BottomNavRoute.Repository,
                BottomNavRoute.Settings,
            )
        }
    }

    val startDestination by remember(isRoot, userPreferences.homepage) {
        derivedStateOf {
            if (isRoot) {
                return@derivedStateOf when (userPreferences.homepage) {
                    Homepage.Home -> BottomNavRoute.Home.route
                    Homepage.Repositories -> BottomNavRoute.Repository.route
                    Homepage.Modules -> BottomNavRoute.Modules.route
                }
            }

            return@derivedStateOf when (userPreferences.homepage) {
                Homepage.Home -> BottomNavRoute.Home.route
                Homepage.Repositories -> BottomNavRoute.Repository.route
                else -> BottomNavRoute.Home.route
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNav(mainScreens)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.none
    ) { paddingValues ->
        CompositionLocalProvider(
            LocalSnackbarHost provides snackbarHostState,
        ) {
            NavHost(
                modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
                navController = navController,
                startDestination = startDestination
            ) {
                homeScreen()
                repositoryScreen(bulkInstallViewModel = bulkInstallViewModel)
                if (isRoot) {
                    modulesScreen()
                }
                settingsScreen()
            }
        }
    }
}

@Composable
private fun BottomNav(
    mainScreens: List<BottomNavRoute>,
) {
    val navController = LocalNavController.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = Modifier
            .imePadding()
            .clip(
                RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp
                )
            )
    ) {
        mainScreens.forEach { screen ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (selected) {
                                screen.iconFilled
                            } else {
                                screen.icon
                            }
                        ),
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = screen.label),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    navController.navigatePopUpTo(
                        route = screen.route,
                        restoreState = !selected
                    )
                }
            )
        }
    }
}
