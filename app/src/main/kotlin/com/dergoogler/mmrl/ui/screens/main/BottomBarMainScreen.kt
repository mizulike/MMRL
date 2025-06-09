package com.dergoogler.mmrl.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
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
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dergoogler.mmrl.datastore.model.Homepage
import com.dergoogler.mmrl.datastore.model.WorkingMode.Companion.isRoot
import com.dergoogler.mmrl.ext.currentScreenWidth
import com.dergoogler.mmrl.ext.navigatePopUpTo
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.ui.component.TopAppBar
import com.dergoogler.mmrl.ui.component.TopAppBarEventIcon
import com.dergoogler.mmrl.ui.component.scaffold.ResponsiveScaffold
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.toolbar.ToolbarEventIcon
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
    val isRoot = userPreferences.workingMode.isRoot && PlatformManager.isAlive

    val width = currentScreenWidth()
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

    if (width.isLarge) {
        Scaffold(
            contentWindowInsets = WindowInsets.none
        ) { paddingValues ->
            val navController = LocalNavController.current

            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier
                            .width(240.dp)
                    ) {
                        TopAppBar(
                            title = {
                                TopAppBarEventIcon()
                            },
                        )

                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                items = mainScreens,
                                key = { it.route }
                            ) { screen ->
                                val selected = navController.isSelected(screen)

                                NavigationDrawerItem(
                                    icon = {
                                        BaseNavIcon(screen, selected, updates)
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(id = screen.label),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    },
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
                }
            ) {
                CurrentNavHost(
                    paddingValues = paddingValues,
                    startDestination = startDestination,
                    isRoot = isRoot
                )
            }
        }

        return
    }

    ResponsiveScaffold(
        bottomBar = {
            BottomNav(
                mainScreens = mainScreens,
                updates = updates
            )
        },
        railBar = {
            RailNav(
                mainScreens = mainScreens,
                updates = updates
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { paddingValues ->
        CurrentNavHost(
            paddingValues = paddingValues,
            startDestination = startDestination,
            isRoot = isRoot
        )
    }
}

@Composable
private fun CurrentNavHost(
    paddingValues: PaddingValues,
    startDestination: String,
    isRoot: Boolean,
) {
    val navController = LocalNavController.current

    NavHost(
        modifier = Modifier.padding(paddingValues),
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

@Composable
private fun BottomNav(
    mainScreens: List<BottomNavRoute>,
    updates: Int,
) {
    val navController = LocalNavController.current

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
            val selected = navController.isSelected(screen)

            NavigationBarItem(
                icon = {
                    BaseNavIcon(screen, selected, updates)
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

@Composable
private fun RailNav(
    mainScreens: List<BottomNavRoute>,
    updates: Int,
) {
    val navController = LocalNavController.current

    NavigationRail(
        header = {
            TopAppBarEventIcon()
        }
    ) {
        mainScreens.forEach { screen ->
            val selected = navController.isSelected(screen)

            NavigationRailItem(
                icon = {
                    BaseNavIcon(screen, selected, updates)
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

@Composable
private fun NavHostController.currentDestination(): NavDestination? {
    val navBackStackEntry by this.currentBackStackEntryAsState()
    return navBackStackEntry?.destination
}

@Composable
private fun NavHostController.isSelected(screen: BottomNavRoute): Boolean {
    val currentDestination = this.currentDestination()
    return currentDestination?.hierarchy?.any { it.route == screen.route } == true
}

@Composable
private fun BaseNavIcon(screen: BottomNavRoute, selected: Boolean, updates: Int) {
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

        return
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
}