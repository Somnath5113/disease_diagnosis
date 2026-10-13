package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.TriageRecordEntity
import com.example.data.model.ConsultationTranscriptItem
import com.example.ui.theme.BorderEmergencyRed
import com.example.ui.theme.BorderRoutineBlue
import com.example.ui.theme.BorderSelfCareGreen
import com.example.ui.theme.BorderUrgentOrange
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CrispCanvasBackground
import com.example.ui.theme.DeepEmergencyRed
import com.example.ui.theme.DeepRoutineBlue
import com.example.ui.theme.DeepSelfCareGreen
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.DeepTextBody
import com.example.ui.theme.DeepTextHeadlines
import com.example.ui.theme.DeepUrgentOrange
import com.example.ui.theme.LightEmergencyContainer
import com.example.ui.theme.LightRoutineContainer
import com.example.ui.theme.LightSelfCareContainer
import com.example.ui.theme.LightUrgentContainer
import com.example.ui.theme.MutedTextSlate
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftAquaContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    records: List<TriageRecordEntity>,
    onBack: () -> Unit,
    onDeleteRecord: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    onShareRecord: (TriageRecordEntity) -> Unit,
    parseTranscript: (String) -> List<ConsultationTranscriptItem>,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    var selectedRecordForDetail by remember { mutableStateOf<TriageRecordEntity?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CrispCanvasBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Assessment History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepTextHeadlines
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("history_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DeepTextHeadlines
                        )
                    }
                },
                actions = {
                    if (records.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = DeepEmergencyRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhiteSurface
                )
            )
        },
        modifier = modifier.testTag("history_screen")
    ) { innerPadding ->
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(SoftAquaContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No consultations",
                            tint = DeepTealPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "No Past Consultations",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepTextHeadlines
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your completed clinical assessments are automatically recorded here for doctor reference.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedTextSlate,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepTealPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Start First Intake", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    CrispHistoryItemCard(
                        record = record,
                        onClick = { selectedRecordForDetail = record },
                        onShare = { onShareRecord(record) },
                        onDelete = { onDeleteRecord(record.id) }
                    )
                }
            }
        }
    }

    selectedRecordForDetail?.let { record ->
        val transcript = parseTranscript(record.transcriptJson)
        CrispRecordDetailDialog(
            record = record,
            transcript = transcript,
            onDismiss = { selectedRecordForDetail = null },
            onShare = { onShareRecord(record) }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Intake History?", color = DeepTextHeadlines, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all stored triage records from this device.", color = DeepTextBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DeepEmergencyRed)
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = MutedTextSlate)
                }
            }
        )
    }
}

@Composable
fun CrispHistoryItemCard(
    record: TriageRecordEntity,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateText = dateFormat.format(Date(record.timestamp))

    val (containerColor, onContainerColor, borderColor) = when (record.urgencyLevel) {
        "EMERGENCY" -> Triple(LightEmergencyContainer, DeepEmergencyRed, BorderEmergencyRed)
        "URGENT_CARE" -> Triple(LightUrgentContainer, DeepUrgentOrange, BorderUrgentOrange)
        "ROUTINE_DOCTOR" -> Triple(LightRoutineContainer, DeepRoutineBlue, BorderRoutineBlue)
        else -> Triple(LightSelfCareContainer, DeepSelfCareGreen, BorderSelfCareGreen)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("history_record_card_${record.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedTextSlate
                )

                Surface(
                    color = containerColor,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Text(
                        text = when (record.urgencyLevel) {
                            "EMERGENCY" -> "Emergency"
                            "URGENT_CARE" -> "Urgent"
                            "ROUTINE_DOCTOR" -> "Routine"
                            else -> "Self-Care"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = onContainerColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = record.chiefComplaint,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DeepTextHeadlines
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Specialty: ${record.category}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DeepTealPrimary
            )

            if (record.likelyConditions.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Matched: ${record.likelyConditions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedTextSlate,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = DeepTealPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MutedTextSlate,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CrispRecordDetailDialog(
    record: TriageRecordEntity,
    transcript: List<ConsultationTranscriptItem>,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    val dateText = dateFormat.format(Date(record.timestamp))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, CardBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("record_detail_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Clinical Intake Record",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DeepTextHeadlines
                    )
                    Text(
                        text = "$dateText • ${record.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedTextSlate
                    )
                }

                item {
                    Surface(
                        color = SoftAquaContainer,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DeepTealPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Chief Symptom / Complaint",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepTealPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = record.chiefComplaint,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = DeepTextHeadlines
                            )
                        }
                    }
                }

                if (record.likelyConditions.isNotBlank()) {
                    item {
                        Text(
                            text = "Potential Conditions:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = DeepTextHeadlines
                        )
                        Text(
                            text = record.likelyConditions,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepTextBody
                        )
                    }
                }

                item {
                    Text(
                        text = "Clinical Guidance:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = DeepTextHeadlines
                    )
                    Text(
                        text = record.clinicalAdvice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepTextBody,
                        lineHeight = 20.sp
                    )
                }

                if (transcript.isNotEmpty()) {
                    item {
                        Text(
                            text = "Multi-turn Transcript Log:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = DeepTextHeadlines
                        )
                    }

                    items(transcript) { item ->
                        Surface(
                            color = CrispCanvasBackground,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = item.question,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepTextHeadlines
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "→ Answer: ${item.answer}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = DeepTealPrimary
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close", color = DeepTextBody)
                        }
                        Button(
                            onClick = onShare,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepTealPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }
                    }
                }
            }
        }
    }
}
