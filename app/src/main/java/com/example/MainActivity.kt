package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.PopupLauncherApp
import com.example.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {
    
    // Manual injection using standard ViewModel Factory, extremely fast and lightweight
    private val viewModel: LauncherViewModel by viewModels {
        val app = application as LauncherApplication
        LauncherViewModel.provideFactory(app.repository, app.preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge layout is mandatory for premium look and safe insets
        enableEdgeToEdge()
        
        setContent {
            PopupLauncherApp(
                viewModel = viewModel,
                onCloseApp = {
                    // Close the activity immediately to completely exit the launcher app
                    finish()
                }
            )
        }
    }
}
