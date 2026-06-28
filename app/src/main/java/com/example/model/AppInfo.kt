package com.example.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
)
