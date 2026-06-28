package com.example.ui

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppInfo
import com.example.repository.SortType
import com.example.viewmodel.LauncherScreen
import com.example.viewmodel.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val searchSelectionQuery by viewModel.selectionSearchQuery.collectAsState()
    val sortType by viewModel.selectionSortType.collectAsState()
    val installedApps by viewModel.sortedAndFilteredInstalledApps.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Select Apps, 1 = Manual Reorder
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Apps") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(LauncherScreen.POPUP) },
                        modifier = Modifier.testTag("back_to_popup_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Popup"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(LauncherScreen.SETTINGS) },
                        modifier = Modifier.testTag("navigate_to_popup_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Popup Visual Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Select Apps") },
                    icon = { Icon(Icons.Default.AppRegistration, contentDescription = null) },
                    modifier = Modifier.testTag("select_apps_tab")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Reorder (${favorites.size})") },
                    icon = { Icon(Icons.Default.Sort, contentDescription = null) },
                    modifier = Modifier.testTag("reorder_tab")
                )
            }

            if (activeTab == 0) {
                // Select Apps Tab
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search and Sort controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = searchSelectionQuery,
                            onValueChange = { viewModel.updateSelectionSearch(it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("selection_search_input"),
                            placeholder = { Text("Search installed apps...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchSelectionQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSelectionSearch("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Sorting trigger
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .size(56.dp)
                                    .testTag("sort_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Sort Options",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Alphabetical (A-Z)") },
                                    leadingIcon = { Icon(Icons.Default.SortByAlpha, null) },
                                    trailingIcon = { if (sortType == SortType.ALPHABETICAL_AZ) Icon(Icons.Default.Check, null) else null },
                                    onClick = {
                                        viewModel.changeSortType(SortType.ALPHABETICAL_AZ)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Alphabetical (Z-A)") },
                                    leadingIcon = { Icon(Icons.Default.SortByAlpha, null) },
                                    trailingIcon = { if (sortType == SortType.ALPHABETICAL_ZA) Icon(Icons.Default.Check, null) else null },
                                    onClick = {
                                        viewModel.changeSortType(SortType.ALPHABETICAL_ZA)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Recently Updated") },
                                    leadingIcon = { Icon(Icons.Default.Update, null) },
                                    trailingIcon = { if (sortType == SortType.RECENTLY_UPDATED) Icon(Icons.Default.Check, null) else null },
                                    onClick = {
                                        viewModel.changeSortType(SortType.RECENTLY_UPDATED)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Installation Date") },
                                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                                    trailingIcon = { if (sortType == SortType.INSTALL_DATE) Icon(Icons.Default.Check, null) else null },
                                    onClick = {
                                        viewModel.changeSortType(SortType.INSTALL_DATE)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Most Recently Used") },
                                    leadingIcon = { Icon(Icons.Default.History, null) },
                                    trailingIcon = { if (sortType == SortType.MOST_RECENT_USED) Icon(Icons.Default.Check, null) else null },
                                    onClick = {
                                        viewModel.changeSortType(SortType.MOST_RECENT_USED)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Bulk selection actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.selectAll() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_all_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.SelectAll, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select All")
                        }

                        Button(
                            onClick = { viewModel.deselectAll() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("deselect_all_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Default.Deselect, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear All")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // App list with checkboxes
                    if (installedApps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No installed apps match your query.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("installed_apps_list"),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            itemsIndexed(installedApps, key = { _, app -> app.packageName }) { _, app ->
                                val isSelected = favorites.contains(app.packageName)
                                SelectionAppRow(
                                    app = app,
                                    isSelected = isSelected,
                                    onToggle = { viewModel.toggleFavorite(app.packageName) },
                                    getIcon = { viewModel.getAppIcon(app.packageName) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Manual Reorder Tab
                val favoriteApps = remember(favorites, installedApps) {
                    favorites.mapNotNull { pkg -> installedApps.find { it.packageName == pkg } }
                }

                if (favoriteApps.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AppRegistration,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No favorites selected yet!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Go to the 'Select Apps' tab to choose which apps appear in the popup launcher.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("reorder_favorites_list"),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        itemsIndexed(favoriteApps, key = { _, app -> app.packageName }) { index, app ->
                            ReorderAppRow(
                                app = app,
                                index = index,
                                totalCount = favoriteApps.size,
                                onMoveUp = { viewModel.moveFavorite(index, index - 1) },
                                onMoveDown = { viewModel.moveFavorite(index, index + 1) },
                                getIcon = { viewModel.getAppIcon(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionAppRow(
    app: AppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit,
    getIcon: () -> Drawable?,
    modifier: Modifier = Modifier
) {
    val appIcon = remember(app.packageName) { getIcon() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("selection_row_${app.packageName}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (appIcon != null) {
                androidx.compose.foundation.Image(
                    painter = DrawablePainter(appIcon),
                    contentDescription = app.label,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = app.label.take(1).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            modifier = Modifier.testTag("checkbox_${app.packageName}")
        )
    }
}

@Composable
fun ReorderAppRow(
    app: AppInfo,
    index: Int,
    totalCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    getIcon: () -> Drawable?,
    modifier: Modifier = Modifier
) {
    val appIcon = remember(app.packageName) { getIcon() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("reorder_row_${app.packageName}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (appIcon != null) {
                androidx.compose.foundation.Image(
                    painter = DrawablePainter(appIcon),
                    contentDescription = app.label,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = app.label.take(1).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // App metadata
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Position: ${index + 1}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Reordering arrows
        Row {
            IconButton(
                onClick = onMoveUp,
                enabled = index > 0,
                modifier = Modifier.testTag("move_up_${app.packageName}")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Move Up",
                    tint = if (index > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }

            IconButton(
                onClick = onMoveDown,
                enabled = index < totalCount - 1,
                modifier = Modifier.testTag("move_down_${app.packageName}")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Move Down",
                    tint = if (index < totalCount - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }
        }
    }
}
