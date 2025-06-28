package com.dergoogler.mmrl.ui.navigation.graphs

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dergoogler.mmrl.ui.navigation.BottomNavRoute
import com.dergoogler.mmrl.ui.screens.settings.SettingsScreen
import com.dergoogler.mmrl.ui.screens.settings.appearance.AppearanceScreen
import com.dergoogler.mmrl.ui.screens.settings.appearance.screens.AppThemeScreen
import com.dergoogler.mmrl.ui.screens.settings.blacklist.BlacklistScreen
import com.dergoogler.mmrl.ui.screens.settings.changelogs.ChangelogScreen
import com.dergoogler.mmrl.ui.screens.settings.developer.DeveloperScreen
import com.dergoogler.mmrl.ui.screens.settings.logviewer.LogScreen
import com.dergoogler.mmrl.ui.screens.settings.modules.ModulesScreen
import com.dergoogler.mmrl.ui.screens.settings.other.OtherScreen
import com.dergoogler.mmrl.ui.screens.settings.security.SecurityScreen
import com.dergoogler.mmrl.ui.screens.settings.terminal.TerminalScreen
import com.dergoogler.mmrl.ui.screens.settings.updates.UpdatesScreen

enum class SettingsScreen(val route: String) {
    Home("Settings_Settings"),
    Appearance("Settings_Appearance"),
    Updates("Settings_Updates"),
    Security("Settings_Security"),
    Modules("Settings_Modules"),
    Other("Settings_Other"),
    Blacklist("Settings_Blacklist"),
    Changelog("Settings_Changelog"),
    Developer("Settings_Developer"),
    LogViewer("Settings_LogViewer"),
    AppTheme("Settings_AppTheme"),
    Terminal("Settings_Terminal")
}

fun NavGraphBuilder.settingsScreen() = navigation(
    startDestination = SettingsScreen.Home.route,
    route = BottomNavRoute.Settings.route
) {
    composable(
        route = SettingsScreen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        SettingsScreen()
    }

    composable(
        route = SettingsScreen.Updates.route,
        enterTransition = { scaleIn() + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        UpdatesScreen()
    }

    composable(
        route = SettingsScreen.Appearance.route,
        enterTransition = { scaleIn() + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        AppearanceScreen()
    }

    composable(
        route = SettingsScreen.Security.route,
        enterTransition = { scaleIn() + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        SecurityScreen()
    }

    composable(
        route = SettingsScreen.Modules.route,
        enterTransition = { scaleIn() + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        ModulesScreen()
    }

    composable(
        route = SettingsScreen.Other.route,
        enterTransition = { scaleIn() + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        OtherScreen()
    }

    composable(
        route = SettingsScreen.Blacklist.route,
        enterTransition = { scaleIn() + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        BlacklistScreen()
    }

    composable(
        route = SettingsScreen.Changelog.route,
        enterTransition = { scaleIn() + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        ChangelogScreen()
    }

    composable(
        route = SettingsScreen.Developer.route,
        enterTransition = { scaleIn() + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        DeveloperScreen()
    }

    composable(
        route = SettingsScreen.LogViewer.route,
        enterTransition = { scaleIn() + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        LogScreen()
    }

    composable(
        route = SettingsScreen.AppTheme.route,
        enterTransition = { scaleIn() + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        AppThemeScreen()
    }

    composable(
        route = SettingsScreen.Terminal.route,
        enterTransition = { scaleIn() + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        TerminalScreen()
    }
}