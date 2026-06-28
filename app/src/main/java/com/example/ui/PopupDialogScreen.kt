package com.example.ui

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfo
import com.example.model.PopupSettings
import com.example.viewmodel.LauncherScreen
import com.example.viewmodel.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopupDialogScreen(
    viewModel: LauncherViewModel,
    onCloseApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.popupSettings.collectAsState()
    val favorites by viewModel.favoriteAppsInPopup.collectAsState()
    val searchQuery by viewModel.popupSearchQuery.collectAsState()
    val context = LocalContext.current

    // Screen configuration measurements
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Calculate dimensions based on settings percentage
    val dialogWidth = screenWidth * (settings.popupWidthPercent / 100f)
    val dialogHeight = screenHeight * (settings.popupHeightPercent / 100f)

    var isVisible by remember { mutableStateOf(false) }

    // Animate visibility entering
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tapping outside the dialog exits the launcher
                onCloseApp()
            },
        contentAlignment = Alignment.Center
    ) {
        // Define animation enter/exit states
        val enterTransition = when (settings.animationType) {
            "none" -> EnterTransition.None
            "fade" -> fadeIn(animationSpec = tween(durationMillis = 200))
            "slide" -> slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(durationMillis = 200))
            else -> scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(durationMillis = 200))
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = enterTransition,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consume taps on card to prevent close */ }
        ) {
            Card(
                modifier = Modifier
                    .width(dialogWidth)
                    .height(dialogHeight)
                    .testTag("popup_dialog_card"),
                shape = RoundedCornerShape(settings.cornerRadiusDp.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Header Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Apps Launcher",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.navigateTo(LauncherScreen.SELECTION) },
                                modifier = Modifier.testTag("settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Open Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onCloseApp() },
                                modifier = Modifier.testTag("close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close App",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Optional Search Bar
                    if (settings.showSearchBar) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updatePopupSearch(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("popup_search_input"),
                            placeholder = { Text("Search favorite apps...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updatePopupSearch("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear Search")
                                    }
                                }
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Favorites Grid list or Empty State
                    if (favorites.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No apps selected yet!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap the gear icon above to select your favorite applications.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.navigateTo(LauncherScreen.SELECTION) },
                                    modifier = Modifier.testTag("configure_favorites_button")
                                ) {
                                    Text("Select Apps")
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(settings.columns),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("popup_apps_grid"),
                            contentPadding = PaddingValues(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(favorites, key = { it.packageName }) { app ->
                                PopupAppItem(
                                    app = app,
                                    settings = settings,
                                    onAppClick = {
                                        viewModel.launchApplication(app.packageName) {
                                            // Auto close app upon successful launch
                                            onCloseApp()
                                        }
                                    },
                                    getIcon = { viewModel.getAppIcon(app.packageName) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PopupAppItem(
    app: AppInfo,
    settings: PopupSettings,
    onAppClick: () -> Unit,
    getIcon: () -> Drawable?,
    modifier: Modifier = Modifier
) {
    val appIcon = remember(app.packageName) { getIcon() }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onAppClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("app_item_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(settings.iconSizeDp.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (appIcon != null) {
                androidx.compose.foundation.Image(
                    painter = DrawablePainter(appIcon),
                    contentDescription = app.label,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback letter if icon fails to load
                Text(
                    text = app.label.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        if (settings.showAppNames) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = settings.textSizeSp.sp,
                    lineHeight = (settings.textSizeSp + 2).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
