package com.example.model

data class PopupSettings(
    val columns: Int = 4,
    val iconSizeDp: Int = 48,
    val textSizeSp: Int = 12,
    val popupWidthPercent: Int = 85,
    val popupHeightPercent: Int = 65,
    val cornerRadiusDp: Int = 16,
    val animationType: String = "scale", // "none", "fade", "scale", "slide"
    val theme: String = "system",       // "light", "dark", "system"
    val showAppNames: Boolean = true,
    val showSearchBar: Boolean = true
)
