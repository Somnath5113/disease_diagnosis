package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryItem
import com.example.data.model.TriageNode
import com.example.data.model.TriageOption
import com.example.ui.components.EmergencyDialog
import com.example.ui.theme.BorderEmergencyRed
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CrispCanvasBackground
import com.example.ui.theme.DeepEmergencyRed
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.DeepTextBody
import com.example.ui.theme.DeepTextHeadlines
import com.example.ui.theme.LightEmergencyContainer
import com.example.ui.theme.MutedTextSlate
import com.example.ui.theme.OnDeepTealPrimary
import com.example.ui.theme.OnLightEmergencyContainer
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftAquaContainer
import com.example.ui.viewmodel.ConsultationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationScreen(
    state: ConsultationState,
    onOptionSelected: (TriageOption) -> Unit,
    onBacktrack: () -> Unit,
    onExitConsultation: () -> Unit,
    onAcknowledgeEmergency: () -> Unit,
    onDismissEmergencyModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBacktrack()
    }

    val currentNode = state.currentNode
    val category = state.category ?: CategoryItem("", "Intake", "", "", "")
    val stepCount = state.stepHistory.size + 1

    Scaffold(
        containerColor = CrispCanvasBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepTextHeadlines,
                            maxLines = 1
                        )
                        Text(
                            text = "Question $stepCount • Clinical Decision Tree",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = DeepTealPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBacktrack,
                        modifier = Modifier.testTag("consultation_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Question",
                            tint = DeepTextHeadlines
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onExitConsultation,
                        modifier = Modifier.testTag("consultation_exit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Intake",
                            tint = MutedTextSlate
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhiteSurface
                )
            )
        },
        modifier = modifier.testTag("consultation_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Smooth Deep Teal Progress Bar
            val targetProgress = (stepCount / 6f).coerceIn(0.15f, 0.95f)
            val animatedProgress by animateFloatAsState(
                targetValue = targetProgress,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                label = "progress_anim"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .testTag("consultation_progress_bar"),
                color = DeepTealPrimary,
                trackColor = SoftAquaContainer
            )

            if (currentNode != null) {
                AnimatedContent(
                    targetState = currentNode,
                    transitionSpec = {
                        slideInHorizontally(
                            animationSpec = tween(280, easing = FastOutSlowInEasing),
                            initialOffsetX = { width -> width / 3 }
                        ) + fadeIn(animationSpec = tween(240)) togetherWith
                                slideOutHorizontally(
                                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                                    targetOffsetX = { width -> -width / 3 }
                                ) + fadeOut(animationSpec = tween(200))
                    },
                    label = "smooth_node_transition"
                ) { targetNode ->
                    CrispQuestionContent(
                        node = targetNode,
                        stepCount = stepCount,
                        onOptionSelected = onOptionSelected
                    )
                }
            }
        }
    }

    if (state.isRedFlagAlertVisible && state.pendingRedFlagOption != null) {
        EmergencyDialog(
            option = state.pendingRedFlagOption,
            onAcknowledgeAndProceed = onAcknowledgeEmergency,
            onDismiss = onDismissEmergencyModal
        )
    }
}

@Composable
private fun CrispQuestionContent(
    node: TriageNode,
    stepCount: Int,
    onOptionSelected: (TriageOption) -> Unit
) {
    var isExplanationExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pure White Question Card with Deep Teal Badge
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("question_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(SoftAquaContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = "Doctor",
                                    tint = DeepTealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Physician Consultation Query",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepTealPrimary
                            )
                        }

                        Surface(
                            color = SoftAquaContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Q$stepCount",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepTealPrimary,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = node.question,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 28.sp
                        ),
                        color = DeepTextHeadlines,
                        modifier = Modifier.testTag("question_text")
                    )

                    if (!node.explanation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = SoftAquaContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DeepTealPrimary.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { isExplanationExpanded = !isExplanationExpanded }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.HelpOutline,
                                            contentDescription = "Rationale",
                                            tint = DeepTealPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Clinical Rationale",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = DeepTealPrimary
                                        )
                                    }
                                    Text(
                                        text = if (isExplanationExpanded) "Hide" else "Learn more",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = DeepTealPrimary
                                    )
                                }

                                if (isExplanationExpanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = node.explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DeepTextBody,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Prompt
        item {
            Text(
                text = "Select the answer that best fits your symptom:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = DeepTextHeadlines
            )
        }

        // Response Option Cards with Deep Indicators
        items(node.options, key = { it.text }) { option ->
            CrispOptionCard(
                option = option,
                onSelect = { onOptionSelected(option) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CrispOptionCard(
    option: TriageOption,
    onSelect: () -> Unit
) {
    val borderColor = if (option.isRedFlag) {
        BorderEmergencyRed
    } else {
        CardBorderSubtle
    }

    val backgroundColor = if (option.isRedFlag) {
        LightEmergencyContainer.copy(alpha = 0.5f)
    } else {
        PureWhiteSurface
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onSelect)
            .testTag("option_card_${option.text.take(15).lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio Indicator Ring
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(
                        width = 2.5.dp,
                        color = if (option.isRedFlag) DeepEmergencyRed else DeepTealPrimary,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    ),
                    color = if (option.isRedFlag) DeepEmergencyRed else DeepTextHeadlines
                )

                if (!option.subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = option.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedTextSlate,
                        lineHeight = 18.sp
                    )
                }

                if (option.isRedFlag) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = LightEmergencyContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderEmergencyRed)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Emergency Flag",
                                tint = DeepEmergencyRed,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Emergency Red Flag Alert",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnLightEmergencyContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
