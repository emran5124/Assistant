package com.example.viewmodel

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.LauncherPreferences
import com.example.model.AppInfo
import com.example.model.PopupSettings
import com.example.repository.AppRepository
import com.example.repository.SortType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LauncherScreen {
    POPUP,
    SELECTION,
    SETTINGS
}

class LauncherViewModel(
    private val repository: AppRepository,
    private val preferences: LauncherPreferences
) : ViewModel() {

    // Current navigation state
    private val _currentScreen = MutableStateFlow(LauncherScreen.POPUP)
    val currentScreen: StateFlow<LauncherScreen> = _currentScreen.asStateFlow()

    // Query for app selection screen
    private val _selectionSearchQuery = MutableStateFlow("")
    val selectionSearchQuery: StateFlow<String> = _selectionSearchQuery.asStateFlow()

    // Query for popup filter
    private val _popupSearchQuery = MutableStateFlow("")
    val popupSearchQuery: StateFlow<String> = _popupSearchQuery.asStateFlow()

    // Active sort option for installed applications
    private val _selectionSortType = MutableStateFlow(SortType.ALPHABETICAL_AZ)
    val selectionSortType: StateFlow<SortType> = _selectionSortType.asStateFlow()

    // Raw installed application list loaded from package manager
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    // App favorites and settings loaded from DataStore
    val favorites: StateFlow<List<String>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val popupSettings: StateFlow<PopupSettings> = preferences.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PopupSettings())

    val launchHistory: StateFlow<Map<String, Long>> = preferences.launchHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = repository.getInstalledApps()
        }
    }

    // Set active screen
    fun navigateTo(screen: LauncherScreen) {
        _currentScreen.value = screen
        // Reset query when navigating
        _popupSearchQuery.value = ""
        _selectionSearchQuery.value = ""
    }

    // Update query for selection list
    fun updateSelectionSearch(query: String) {
        _selectionSearchQuery.value = query
    }

    // Update query for popup filtering
    fun updatePopupSearch(query: String) {
        _popupSearchQuery.value = query
    }

    // Change sorting mode
    fun changeSortType(sortType: SortType) {
        _selectionSortType.value = sortType
    }

    // Filter and sort the full list of installed apps for the Selection Screen
    val sortedAndFilteredInstalledApps: StateFlow<List<AppInfo>> = combine(
        _installedApps,
        favorites,
        launchHistory,
        _selectionSortType,
        _selectionSearchQuery
    ) { apps, favs, history, sortType, query ->
        val filtered = if (query.isBlank()) {
            apps
        } else {
            apps.filter { it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
        }
        repository.sortApps(filtered, sortType, favs, history)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Get favorite applications to display inside the Popup, filtered by popup search query if any
    val favoriteAppsInPopup: StateFlow<List<AppInfo>> = combine(
        _installedApps,
        favorites,
        launchHistory,
        _popupSearchQuery
    ) { apps, favs, history, query ->
        val favApps = favs.mapNotNull { favPkg ->
            apps.find { it.packageName == favPkg }
        }
        if (query.isBlank()) {
            favApps
        } else {
            favApps.filter { it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Toggle favorite selection status
    fun toggleFavorite(packageName: String) {
        viewModelScope.launch {
            val currentFavs = favorites.value.toMutableList()
            if (currentFavs.contains(packageName)) {
                currentFavs.remove(packageName)
            } else {
                currentFavs.add(packageName)
            }
            repository.saveFavorites(currentFavs)
        }
    }

    // Drag and drop / swap reordering helper
    fun moveFavorite(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val currentFavs = favorites.value.toMutableList()
            if (fromIndex in currentFavs.indices && toIndex in currentFavs.indices) {
                val moved = currentFavs.removeAt(fromIndex)
                currentFavs.add(toIndex, moved)
                repository.saveFavorites(currentFavs)
            }
        }
    }

    // Select all launchable applications
    fun selectAll() {
        viewModelScope.launch {
            val allPackages = _installedApps.value.map { it.packageName }
            repository.saveFavorites(allPackages)
        }
    }

    // Deselect all launchable applications
    fun deselectAll() {
        viewModelScope.launch {
            repository.saveFavorites(emptyList())
        }
    }

    // Load Drawable icon on-the-fly (extremely lightweight resource lookup)
    fun getAppIcon(packageName: String): Drawable? {
        return repository.getAppIcon(packageName)
    }

    // Launch application and record launch timestamp
    fun launchApplication(packageName: String, onLaunchSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.recordAppLaunch(packageName)
            val success = repository.launchApp(packageName)
            if (success) {
                onLaunchSuccess()
            }
        }
    }

    // Settings adjustments
    fun updateColumns(columns: Int) {
        viewModelScope.launch { preferences.saveColumns(columns.coerceIn(2, 6)) }
    }

    fun updateIconSize(sizeDp: Int) {
        viewModelScope.launch { preferences.saveIconSize(sizeDp.coerceIn(32, 72)) }
    }

    fun updateTextSize(sizeSp: Int) {
        viewModelScope.launch { preferences.saveTextSize(sizeSp.coerceIn(8, 20)) }
    }

    fun updatePopupWidth(widthPercent: Int) {
        viewModelScope.launch { preferences.savePopupWidth(widthPercent.coerceIn(50, 95)) }
    }

    fun updatePopupHeight(heightPercent: Int) {
        viewModelScope.launch { preferences.savePopupHeight(heightPercent.coerceIn(30, 90)) }
    }

    fun updateCornerRadius(radiusDp: Int) {
        viewModelScope.launch { preferences.saveCornerRadius(radiusDp.coerceIn(0, 32)) }
    }

    fun updateAnimationType(animationType: String) {
        viewModelScope.launch { preferences.saveAnimationType(animationType) }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch { preferences.saveTheme(theme) }
    }

    fun updateShowAppNames(show: Boolean) {
        viewModelScope.launch { preferences.saveShowAppNames(show) }
    }

    fun updateShowSearchBar(show: Boolean) {
        viewModelScope.launch { preferences.saveShowSearchBar(show) }
    }

    companion object {
        fun provideFactory(
            repository: AppRepository,
            preferences: LauncherPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LauncherViewModel::class.java)) {
                    return LauncherViewModel(repository, preferences) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
