package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ConclusionScreen
import com.example.ui.screens.ConsultationScreen
import com.example.ui.screens.HistoryScreen
import com.example.data.settings.LocalAppSettings
import com.example.data.settings.SettingsRepository
import com.example.data.settings.ThemeMode
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MedTriageTheme
import com.example.ui.viewmodel.TriageViewModel

enum class MainTab {
    INTAKE,
    HISTORY,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    private val triageViewModel: TriageViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SettingsRepository.withSavedLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsRepository = SettingsRepository.get(this)

        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Keep status and navigation bar icons readable when the theme differs from the system.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        DefaultLightScrim,
                        DefaultDarkScrim
                    ) { darkTheme }
                )
                onDispose { }
            }

            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalAppSettings provides settings,
                LocalDensity provides Density(density.density, density.fontScale * settings.textSize.scale)
            ) {
                MedTriageTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MedTriageApp(
                            viewModel = triageViewModel,
                            settingsRepository = settingsRepository
                        )
                    }
                }
            }
        }
    }

    private companion object {
        // Same scrim colours enableEdgeToEdge() uses by default.
        val DefaultLightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DefaultDarkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}

@Composable
fun MedTriageApp(viewModel: TriageViewModel, settingsRepository: SettingsRepository) {
    val context = LocalContext.current
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()

    // Saveable so the Settings tab survives the activity restart that a language change triggers.
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.INTAKE) }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentRecords by viewModel.recentRecords.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val consultationState by viewModel.consultationState.collectAsStateWithLifecycle()

    fun shareSummary(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.share_chooser_title))
        context.startActivity(shareIntent)
    }

    if (consultationState.isActive) {
        if (consultationState.conclusion != null && consultationState.category != null) {
            // Conclusion Screen
            ConclusionScreen(
                category = consultationState.category!!,
                conclusion = consultationState.conclusion!!,
                stepHistory = consultationState.stepHistory,
                isSavedLocally = consultationState.isSavedLocally,
                onReturnHome = {
                    viewModel.resetConsultation()
                },
                onShareSummary = {
                    val summaryText = viewModel.buildSummaryText(
                        categoryName = consultationState.category!!.name,
                        chiefComplaint = consultationState.category!!.name,
                        conclusion = consultationState.conclusion!!,
                        stepHistory = consultationState.stepHistory
                    )
                    shareSummary(summaryText)
                }
            )
        } else {
            // Interactive Multi-turn Consultation Flow
            ConsultationScreen(
                state = consultationState,
                onOptionSelected = { option ->
                    viewModel.onOptionSelected(option)
                },
                onBacktrack = {
                    viewModel.backtrack()
                },
                onExitConsultation = {
                    viewModel.resetConsultation()
                },
                onAcknowledgeEmergency = {
                    viewModel.acknowledgeRedFlagEmergency()
                },
                onDismissEmergencyModal = {
                    viewModel.dismissRedFlagModal()
                }
            )
        }
    } else {
        // Main Tab Shell (Home / History)
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.testTag("main_bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == MainTab.INTAKE,
                        onClick = { selectedTab = MainTab.INTAKE },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == MainTab.INTAKE) Icons.Filled.MedicalServices else Icons.Outlined.MedicalServices,
                                contentDescription = stringResource(R.string.nav_intake)
                            )
                        },
                        label = {
                            Text(
                                stringResource(R.string.nav_intake),
                                fontWeight = if (selectedTab == MainTab.INTAKE) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_intake")
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainTab.HISTORY,
                        onClick = { selectedTab = MainTab.HISTORY },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == MainTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                                contentDescription = stringResource(R.string.nav_history)
                            )
                        },
                        label = {
                            Text(
                                stringResource(R.string.nav_history),
                                fontWeight = if (selectedTab == MainTab.HISTORY) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_history")
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainTab.SETTINGS,
                        onClick = { selectedTab = MainTab.SETTINGS },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == MainTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.nav_settings)
                            )
                        },
                        label = {
                            Text(
                                stringResource(R.string.nav_settings),
                                fontWeight = if (selectedTab == MainTab.SETTINGS) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally(tween(250)) { it / 4 } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally(tween(250)) { -it / 4 } + fadeOut(tween(180)))
                    } else {
                        (slideInHorizontally(tween(250)) { -it / 4 } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally(tween(250)) { it / 4 } + fadeOut(tween(180)))
                    }
                },
                label = "tab_transition",
                modifier = Modifier.padding(innerPadding)
            ) { tab ->
                when (tab) {
                    MainTab.INTAKE -> {
                        HomeScreen(
                            categories = categories,
                            searchQuery = searchQuery,
                            onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                            onCategorySelected = { category ->
                                viewModel.startConsultation(category)
                            },
                            recentRecords = recentRecords,
                            onRecordClicked = { record ->
                                val summary = viewModel.buildSummaryFromRecord(record)
                                shareSummary(summary)
                            },
                            onNavigateToHistory = {
                                selectedTab = MainTab.HISTORY
                            }
                        )
                    }

                    MainTab.HISTORY -> {
                        HistoryScreen(
                            records = allRecords,
                            onBack = { selectedTab = MainTab.INTAKE },
                            onDeleteRecord = { id -> viewModel.deleteRecord(id) },
                            onClearAllHistory = { viewModel.clearAllHistory() },
                            onShareRecord = { record ->
                                val summary = viewModel.buildSummaryFromRecord(record)
                                shareSummary(summary)
                            },
                            parseTranscript = { json ->
                                viewModel.parseTranscript(json)
                            }
                        )
                    }

                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            settings = settings,
                            onLanguageSelected = { language ->
                                settingsRepository.setLanguage(language)
                                // Resources are bound to the activity context, so restart to apply the language.
                                (context as? Activity)?.recreate()
                            },
                            onThemeSelected = settingsRepository::setThemeMode,
                            onTextSizeSelected = settingsRepository::setTextSize,
                            onEmergencyNumberSelected = settingsRepository::setEmergencyNumber,
                            onShowRemediesChanged = settingsRepository::setShowHomeRemedies,
                            onSaveHistoryChanged = settingsRepository::setSaveHistory,
                            onClearHistory = { viewModel.clearAllHistory() }
                        )
                    }
                }
            }
        }
    }
}
