package com.dergoogler.mmrl.ui.screens.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.ui.navigation.BottomNavRoute
import com.dergoogler.mmrl.ui.navigation.graphs.homeScreen
import com.dergoogler.mmrl.ui.navigation.graphs.modulesScreen
import com.dergoogler.mmrl.ui.navigation.graphs.repositoryScreen
import com.dergoogler.mmrl.ui.navigation.graphs.settingsScreen
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.viewmodel.MainViewModel

@Composable
fun BottomBarMainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val userPreferences = LocalUserPreferences.current
    val navController = LocalNavController.current
    val isRoot = userPreferences.workingMode.isRoot && PlatformManager.isAlive

    val updates by viewModel.updatableModuleCount.collectAsState()

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
            BottomNav(
                mainScreens = mainScreens,
                updates = updates
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
            navController = navController,
            startDestination = startDestination
        ) {
            homeScreen()
            repositoryScreen()
            if (isRoot) {
                modulesScreen()
            }
            settingsScreen()
        }
    }
}

@Composable
private fun BottomNav(
    mainScreens: List<BottomNavRoute>,
    updates: Int,
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
                    if (screen == BottomNavRoute.Modules && updates > 0) {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(text = updates.toString())
                            }
                        }
                    ) {
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
                    }

                        return@NavigationBarItem
                    }

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
                        restoreState = false
                    )
                }
            )
        }
    }
}
