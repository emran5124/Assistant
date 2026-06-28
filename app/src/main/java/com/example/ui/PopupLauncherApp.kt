package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LauncherScreen
import com.example.viewmodel.LauncherViewModel

@Composable
fun PopupLauncherApp(
    viewModel: LauncherViewModel,
    onCloseApp: () -> Unit
) {
    val settings by viewModel.popupSettings.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Determine dark theme mode based on preferences settings
    val darkTheme = when (settings.theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    MyApplicationTheme(
        darkTheme = darkTheme,
        dynamicColor = true // Keep Android 12+ dynamic color capabilities enabled
    ) {
        when (currentScreen) {
            LauncherScreen.POPUP -> {
                PopupDialogScreen(
                    viewModel = viewModel,
                    onCloseApp = onCloseApp
                )
            }
            LauncherScreen.SELECTION -> {
                SelectionScreen(
                    viewModel = viewModel
                )
            }
            LauncherScreen.SETTINGS -> {
                SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
