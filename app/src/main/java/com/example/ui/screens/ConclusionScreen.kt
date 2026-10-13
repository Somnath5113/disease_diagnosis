package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryItem
import com.example.data.model.TriageConclusion
import com.example.data.model.UrgencyLevel
import com.example.ui.theme.*
import com.example.ui.viewmodel.ConsultationStep

@Composable
fun ConclusionScreen(
    category: CategoryItem,
    conclusion: TriageConclusion,
    stepHistory: List<ConsultationStep>,
    isSavedLocally: Boolean,
    onReturnHome: () -> Unit,
    onShareSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler {
        onReturnHome()
    }

    val (badgeText, badgeIcon, containerColor, deepTextColor, primaryBadgeColor, borderColor) = when (conclusion.urgencyLevel) {
        UrgencyLevel.EMERGENCY -> Sextuple(
            "CRITICAL EMERGENCY • CALL 911 / 112",
            Icons.Default.Warning,
            LightEmergencyContainer,
            DeepEmergencyRed,
            DeepEmergencyRed,
            BorderEmergencyRed
        )
        UrgencyLevel.URGENT_CARE -> Sextuple(
            "URGENT CARE • SEEK CLINIC WITHIN 12-24H",
            Icons.Default.Warning,
            LightUrgentContainer,
            DeepUrgentOrange,
            DeepUrgentOrange,
            BorderUrgentOrange
        )
        UrgencyLevel.ROUTINE_DOCTOR -> Sextuple(
            "ROUTINE PHYSICIAN EVALUATION",
            Icons.Default.MedicalServices,
            LightRoutineContainer,
            DeepRoutineBlue,
            DeepRoutineBlue,
            BorderRoutineBlue
        )
        UrgencyLevel.SELF_CARE -> Sextuple(
            "SUPPORTIVE AT-HOME MONITORING",
            Icons.Default.CheckCircle,
            LightSelfCareContainer,
            DeepSelfCareGreen,
            DeepSelfCareGreen,
            BorderSelfCareGreen
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("conclusion_screen_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Triage Urgency Banner Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = containerColor),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("conclusion_urgency_card")
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
                        Surface(
                            color = primaryBadgeColor,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = badgeIcon,
                                    contentDescription = badgeText,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        if (isSavedLocally) {
                            Surface(
                                color = PureWhiteSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Saved",
                                        tint = primaryBadgeColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Saved locally",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = deepTextColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Clinical Triage Evaluation",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp
                        ),
                        color = deepTextColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Specialty: ${category.name} • MedDialog Clinical Pathway",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = deepTextColor.copy(alpha = 0.85f)
                    )

                    if (conclusion.urgencyLevel == UrgencyLevel.EMERGENCY) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                                context.startActivity(dialIntent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepEmergencyRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("conclusion_call_emergency_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call 911 / 112 Immediately", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Likely Clinical Considerations
        if (conclusion.likelyConditions.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Potential Clinical Matches",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepTextHeadlines
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Correlation with MedDialog clinical consultation patterns:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTextSlate
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        conclusion.likelyConditions.forEach { condition ->
                            Surface(
                                color = SoftAquaContainer,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DeepTealPrimary.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(DeepTealPrimary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = condition,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = DeepTextHeadlines
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Clinical Assessment & Guidance
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Clinical Assessment & Guidance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepTextHeadlines
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = conclusion.clinicalAdvice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepTextBody,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Supportive Care Instructions
        if (conclusion.homeCareInstructions.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Recommended Care & Symptom Relief",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepTextHeadlines
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        conclusion.homeCareInstructions.forEach { instruction ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(LightSelfCareContainer, CircleShape)
                                        .border(1.dp, BorderSelfCareGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Care step",
                                        tint = DeepSelfCareGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = instruction,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DeepTextBody,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Warning Thresholds Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightUrgentContainer),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, BorderUrgentOrange),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Warning thresholds",
                        tint = DeepUrgentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "When to Seek Immediate Medical Evaluation",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepUrgentOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = conclusion.whenToSeekImmediateHelp,
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepUrgentOrange.copy(alpha = 0.95f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onShareSummary,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepTealPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("share_doctor_summary_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Share Summary with Doctor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = onReturnHome,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepTealPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("return_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Finish & Return to Home",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class Sextuple<A, B, C, D, E, F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F
)
