package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.PopupSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "launcher_preferences")

class LauncherPreferences(private val context: Context) {
    companion object {
        private val KEY_FAVORITES = stringPreferencesKey("favorites")
        private val KEY_LAUNCH_HISTORY = stringPreferencesKey("launch_history")
        private val KEY_COLUMNS = intPreferencesKey("columns")
        private val KEY_ICON_SIZE = intPreferencesKey("icon_size")
        private val KEY_TEXT_SIZE = intPreferencesKey("text_size")
        private val KEY_POPUP_WIDTH = intPreferencesKey("popup_width")
        private val KEY_POPUP_HEIGHT = intPreferencesKey("popup_height")
        private val KEY_CORNER_RADIUS = intPreferencesKey("corner_radius")
        private val KEY_ANIMATION_TYPE = stringPreferencesKey("animation_type")
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_SHOW_APP_NAMES = booleanPreferencesKey("show_app_names")
        private val KEY_SHOW_SEARCH_BAR = booleanPreferencesKey("show_search_bar")
    }

    // Read favorite apps (saved as comma-separated package names)
    val favoritesFlow: Flow<List<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val favString = preferences[KEY_FAVORITES] ?: ""
            if (favString.isEmpty()) emptyList() else favString.split(",")
        }

    // Save favorite apps list
    suspend fun saveFavorites(favorites: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FAVORITES] = favorites.joinToString(",")
        }
    }

    // Read launch history (saved as "package:timestamp,package:timestamp")
    val launchHistoryFlow: Flow<Map<String, Long>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val historyString = preferences[KEY_LAUNCH_HISTORY] ?: ""
            if (historyString.isEmpty()) {
                emptyMap()
            } else {
                historyString.split(",")
                    .mapNotNull {
                        val parts = it.split(":")
                        if (parts.size == 2) {
                            parts[0] to (parts[1].toLongOrNull() ?: 0L)
                        } else null
                    }
                    .toMap()
            }
        }

    // Record app launch in history
    suspend fun recordAppLaunch(packageName: String) {
        context.dataStore.edit { preferences ->
            val currentHistoryString = preferences[KEY_LAUNCH_HISTORY] ?: ""
            val currentMap = if (currentHistoryString.isEmpty()) {
                mutableMapOf<String, Long>()
            } else {
                currentHistoryString.split(",")
                    .mapNotNull {
                        val parts = it.split(":")
                        if (parts.size == 2) {
                            parts[0] to (parts[1].toLongOrNull() ?: 0L)
                        } else null
                    }
                    .toMap()
                    .toMutableMap()
            }

            // Update timestamp for current app
            currentMap[packageName] = System.currentTimeMillis()

            // Save back as string representation
            preferences[KEY_LAUNCH_HISTORY] = currentMap.entries.joinToString(",") {
                "${it.key}:${it.value}"
            }
        }
    }

    // Read popup settings
    val settingsFlow: Flow<PopupSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            PopupSettings(
                columns = preferences[KEY_COLUMNS] ?: 4,
                iconSizeDp = preferences[KEY_ICON_SIZE] ?: 48,
                textSizeSp = preferences[KEY_TEXT_SIZE] ?: 12,
                popupWidthPercent = preferences[KEY_POPUP_WIDTH] ?: 85,
                popupHeightPercent = preferences[KEY_POPUP_HEIGHT] ?: 65,
                cornerRadiusDp = preferences[KEY_CORNER_RADIUS] ?: 16,
                animationType = preferences[KEY_ANIMATION_TYPE] ?: "scale",
                theme = preferences[KEY_THEME] ?: "system",
                showAppNames = preferences[KEY_SHOW_APP_NAMES] ?: true,
                showSearchBar = preferences[KEY_SHOW_SEARCH_BAR] ?: true
            )
        }

    // Update individual settings
    suspend fun saveColumns(columns: Int) {
        context.dataStore.edit { preferences -> preferences[KEY_COLUMNS] = columns }
    }

    suspend fun saveIconSize(sizeDp: Int) {
        context.dataStore.edit { preferences -> preferences[KEY_ICON_SIZE] = sizeDp }
    }

    suspend fun saveTextSize(sizeSp: Int) {
        context.dataStore.edit { preferences -> preferences[KEY_TEXT_SIZE] = sizeSp }
    }

    suspend fun savePopupWidth(widthPercent: Int) {
        context.dataStore.edit { preferences -> preferences[KEY_POPUP_WIDTH] = widthPercent }
    }

    suspend fun savePopupHeight(heightPercent: Int) {
        context.dataStore.edit { preferences -> preferences[KEY_POPUP_HEIGHT] = heightPercent }
    }

    suspend fun saveCornerRadius(radiusDp: Int) {
        context.dataStore.edit { preferences -> preferences[KEY_CORNER_RADIUS] = radiusDp }
    }

    suspend fun saveAnimationType(animationType: String) {
        context.dataStore.edit { preferences -> preferences[KEY_ANIMATION_TYPE] = animationType }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { preferences -> preferences[KEY_THEME] = theme }
    }

    suspend fun saveShowAppNames(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_SHOW_APP_NAMES] = show }
    }

    suspend fun saveShowSearchBar(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_SHOW_SEARCH_BAR] = show }
    }
}
