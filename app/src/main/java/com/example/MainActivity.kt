package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MedicalServices
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ConclusionScreen
import com.example.ui.screens.ConsultationScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MedTriageTheme
import com.example.ui.viewmodel.TriageViewModel

enum class MainTab {
    INTAKE,
    HISTORY
}

class MainActivity : ComponentActivity() {
    private val triageViewModel: TriageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MedTriageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MedTriageApp(viewModel = triageViewModel)
                }
            }
        }
    }
}

@Composable
fun MedTriageApp(viewModel: TriageViewModel) {
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(MainTab.INTAKE) }

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
        val shareIntent = Intent.createChooser(sendIntent, "Share Clinical Intake Summary")
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
                                contentDescription = "Clinical Intake"
                            )
                        },
                        label = {
                            Text(
                                "Clinical Intake",
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
                                contentDescription = "History"
                            )
                        },
                        label = {
                            Text(
                                "History",
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
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState == MainTab.HISTORY) {
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
                }
            }
        }
    }
}
