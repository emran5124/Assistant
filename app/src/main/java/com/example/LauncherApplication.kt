package com.example

import android.app.Application
import com.example.data.LauncherPreferences
import com.example.repository.AppRepository

class LauncherApplication : Application() {
    val preferences: LauncherPreferences by lazy {
        LauncherPreferences(applicationContext)
    }
    
    val repository: AppRepository by lazy {
        AppRepository(applicationContext, preferences)
    }
}
