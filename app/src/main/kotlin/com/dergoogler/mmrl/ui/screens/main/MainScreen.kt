package com.dergoogler.mmrl.ui.screens.main

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.navigation.MainRoute
import com.dergoogler.mmrl.ui.navigation.mainScreen
import com.dergoogler.mmrl.ui.providable.LocalBulkInstall
import com.dergoogler.mmrl.ui.providable.LocalMainNavController
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.initPlatform
import com.dergoogler.mmrl.viewmodel.BulkInstallViewModel

@Composable
fun MainScreen() {
    val mainNavController = LocalMainNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val bulkInstallViewModel: BulkInstallViewModel = hiltViewModel()
    val context = LocalContext.current
    val userPreferences = LocalUserPreferences.current


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        Log.d("MainScreen", "launcher: $result")
    }

    LaunchedEffect(Unit) {
        if (PlatformManager.platform.isNotNonRoot) {
            launcher.launch(
                arrayOf(
                    "com.dergoogler.mmrl.permission.WEBUI_X",
                    "com.dergoogler.mmrl.permission.WEBUI_LEGACY"
                )
            )
        }

        initPlatform(context, userPreferences.workingMode.toPlatform())
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.none,
    ) { paddingValues ->
        CompositionLocalProvider(
            LocalSnackbarHost provides snackbarHostState,
            LocalBulkInstall provides bulkInstallViewModel
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
