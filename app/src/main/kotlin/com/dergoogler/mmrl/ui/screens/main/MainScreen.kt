package com.dergoogler.mmrl.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.ui.component.listItem.ListButtonItem
import com.dergoogler.mmrl.ui.component.listItem.ListHeader
import com.dergoogler.mmrl.ui.navigation.MainRoute
import com.dergoogler.mmrl.ui.navigation.graphs.SettingsScreen
import com.dergoogler.mmrl.ui.navigation.graphs.settingsScreen
import com.dergoogler.mmrl.ui.navigation.mainScreen
import com.dergoogler.mmrl.ui.providable.LocalDrawerState
import com.dergoogler.mmrl.ui.providable.LocalMainNavController
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.screens.home.items.RebootBottomSheet
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val mainNavController = LocalMainNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val browser = LocalUriHandler.current

    val navigate = remember {
        { route: String ->
            scope.launch {
                drawerState.close()
                if (drawerState.isClosed) {
                    mainNavController.navigate(route)
                }
            }
        }
    }

    var openRebootSheet by remember { mutableStateOf(false) }
    if (openRebootSheet) {
        RebootBottomSheet(
            onClose = { openRebootSheet = false })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val maxDrawerWidth = configuration.smallestScreenWidthDp * 0.8f

            Scaffold(
                modifier = Modifier.width(maxDrawerWidth.dp),
            ) { innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    ListHeader(title = R.string.drawer_general)

                    ListButtonItem(
                        icon = R.drawable.settings,
                        title = stringResource(id = R.string.page_settings),
                        onClick = {
                            navigate(SettingsScreen.Home.route)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 0.9.dp
                    )

                    ListHeader(title = R.string.drawer_support)

                    ListButtonItem(
                        icon = R.drawable.info_circle,
                        title = stringResource(id = R.string.settings_about),
                        onClick = {
                            navigate(MainRoute.About.route)
                        }
                    )

                    ListButtonItem(
                        icon = R.drawable.heart,
                        title = stringResource(id = R.string.thank_you),
                        onClick = {
                            navigate(MainRoute.ThankYou.route)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 0.9.dp
                    )

                    ListHeader(title = R.string.drawer_tools)

                    ListButtonItem(
                        enabled = Platform.isAlive,
                        icon = R.drawable.refresh,
                        title = stringResource(id = R.string.reboot),
                        onClick = {
                            openRebootSheet = true
                        }
                    )

                    ListHeader(title = R.string.drawer_legal)


                    ListButtonItem(
                        icon = R.drawable.spy,
                        title = stringResource(id = R.string.settings_privacy_policy),
                        onClick = {
                            browser.openUri(Const.PRIVACY_POLICY_URL)
                        }
                    )

                    ListButtonItem(
                        icon = R.drawable.files,
                        title = stringResource(id = R.string.settings_terms_of_service),
                        onClick = {
                            browser.openUri(Const.TERMS_OF_SERVICE_URL)
                        }
                    )
                }
            }
        },
        content = {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = WindowInsets.none,
            ) { paddingValues ->
                CompositionLocalProvider(
                    LocalSnackbarHost provides snackbarHostState,
                    LocalDrawerState provides drawerState
                ) {
                    NavHost(
                        modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
                        navController = mainNavController,
                        startDestination = MainRoute.BottomNavScreen.route
                    ) {
                        mainScreen()
                        settingsScreen()
                    }
                }
            }
        }
    )
}
