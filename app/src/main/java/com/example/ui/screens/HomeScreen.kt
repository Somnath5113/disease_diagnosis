package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TriageRecordEntity
import com.example.data.model.CategoryItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    categories: List<CategoryItem>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (CategoryItem) -> Unit,
    recentRecords: List<TriageRecordEntity>,
    onRecordClicked: (TriageRecordEntity) -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilterChip by remember { mutableStateOf<String?>(null) }

    val filteredCategories = categories.filter { category ->
        val matchesSearch = searchQuery.isBlank() ||
                category.name.contains(searchQuery, ignoreCase = true) ||
                category.description.contains(searchQuery, ignoreCase = true)

        val matchesChip = selectedFilterChip == null || category.id == selectedFilterChip

        matchesSearch && matchesChip
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Deep Colored Hero Banner with Light Accents
        item {
            val deepHeroGradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF04434D),
                    Color(0xFF075965),
                    Color(0xFF0A6847)
                )
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(deepHeroGradient)
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color.White.copy(alpha = 0.22f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MedicalServices,
                                        contentDescription = "MedTriage",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "MedTriage",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.4).sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Clinical Intake Engine",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = Color(0xFFA5F3FC)
                                    )
                                }
                            }

                            // Glowing Light-Mode Badge
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                modifier = Modifier.testTag("offline_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(Color(0xFF4ADE80), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "100% Offline",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Deterministic, rule-based medical evaluation pathways derived from real-world MedDialog clinical consultations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.95f),
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = Color(0xFF67E8F9),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Zero server dependency • Patient data stays on device",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }
                }
            }
        }

        // Crisp Search Field with Deep Borders
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = {
                    Text(
                        "Search symptoms, body systems, complaints...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedTextSlate
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = DeepTealPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = DeepTextBody
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhiteSurface,
                    unfocusedContainerColor = PureWhiteSurface,
                    focusedBorderColor = DeepTealPrimary,
                    unfocusedBorderColor = CardBorderSubtle,
                    focusedTextColor = DeepTextHeadlines,
                    unfocusedTextColor = DeepTextHeadlines
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("symptom_search_input")
            )
        }

        // Filter Chips with Deep Color Selected State
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilterChip == null,
                    onClick = { selectedFilterChip = null },
                    label = {
                        Text(
                            "All Systems",
                            fontWeight = if (selectedFilterChip == null) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepTealPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = PureWhiteSurface,
                        labelColor = DeepTextBody
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilterChip == null,
                        borderColor = CardBorderSubtle,
                        selectedBorderColor = DeepTealPrimary
                    )
                )

                categories.forEach { cat ->
                    val isSelected = selectedFilterChip == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedFilterChip = if (isSelected) null else cat.id
                        },
                        label = {
                            Text(
                                cat.name.substringBefore(" &"),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepTealPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = PureWhiteSurface,
                            labelColor = DeepTextBody
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = CardBorderSubtle,
                            selectedBorderColor = DeepTealPrimary
                        )
                    )
                }
            }
        }

        // Deep Emergency Hotline Alert Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightEmergencyContainer),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, BorderEmergencyRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emergency_hotline_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(DeepEmergencyRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Emergency Call",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Emergency Quick-Dial",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepEmergencyRed
                            )
                            Text(
                                text = "Immediate access for chest pain, stroke, or choking",
                                style = MaterialTheme.typography.bodySmall,
                                color = DeepEmergencyRed.copy(alpha = 0.85f),
                                maxLines = 1
                            )
                        }
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepEmergencyRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("home_quick_dial_button")
                    ) {
                        Text("Call 911", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Specialty Clinical Pathways",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepTextHeadlines
                )
                Text(
                    text = "${filteredCategories.size} Systems",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepTealPrimary
                )
            }
        }

        // Crisp White Category Cards with Deep Accent Icons
        items(filteredCategories, key = { it.id }) { category ->
            CrispCategoryCard(
                category = category,
                onClick = { onCategorySelected(category) }
            )
        }

        // Recent Intakes Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recent Intake Assessments",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepTextHeadlines
                )
                if (recentRecords.isNotEmpty()) {
                    TextButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("view_all_history_button")
                    ) {
                        Text(
                            "View All (${recentRecords.size})",
                            color = DeepTealPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (recentRecords.isEmpty()) {
            item {
                Surface(
                    color = PureWhiteSurface,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "No assessments",
                            tint = DeepTealPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No saved assessments yet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepTextHeadlines
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap any clinical specialty above to begin your intake consultation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTextSlate,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentRecords, key = { it.id }) { record ->
                CrispRecentRecordCard(
                    record = record,
                    onClick = { onRecordClicked(record) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CrispCategoryCard(
    category: CategoryItem,
    onClick: () -> Unit
) {
    val (icon, gradientColors) = when (category.id) {
        "RESPIRATORY" -> Pair(
            Icons.Default.MedicalServices,
            listOf(Color(0xFF04434D), Color(0xFF075965))
        )
        "GASTROINTESTINAL" -> Pair(
            Icons.Default.Favorite,
            listOf(Color(0xFFC2410C), Color(0xFFEA580C))
        )
        "NEUROLOGICAL" -> Pair(
            Icons.Default.Psychology,
            listOf(Color(0xFF1D4ED8), Color(0xFF2563EB))
        )
        "DERMATOLOGICAL" -> Pair(
            Icons.Default.Healing,
            listOf(Color(0xFF047857), Color(0xFF059669))
        )
        "FEVER" -> Pair(
            Icons.Default.Thermostat,
            listOf(Color(0xFFB91C1C), Color(0xFFDC2626))
        )
        else -> Pair(
            Icons.Default.MedicalServices,
            listOf(Color(0xFF04434D), Color(0xFF0A6847))
        )
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
            .testTag("category_card_${category.id.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Brush.linearGradient(gradientColors), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.name,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepTextHeadlines
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedTextSlate,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(SoftAquaContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    tint = DeepTealPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
fun CrispRecentRecordCard(
    record: TriageRecordEntity,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateText = dateFormat.format(Date(record.timestamp))

    val (containerColor, onContainerColor, borderColor) = when (record.urgencyLevel) {
        "EMERGENCY" -> Triple(LightEmergencyContainer, DeepEmergencyRed, BorderEmergencyRed)
        "URGENT_CARE" -> Triple(LightUrgentContainer, DeepUrgentOrange, BorderUrgentOrange)
        "ROUTINE_DOCTOR" -> Triple(LightRoutineContainer, DeepRoutineBlue, BorderRoutineBlue)
        else -> Triple(LightSelfCareContainer, DeepSelfCareGreen, BorderSelfCareGreen)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("recent_record_item_${record.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.category,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepTealPrimary
                    )
                    Text(
                        text = " • $dateText",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedTextSlate
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.chiefComplaint,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = DeepTextHeadlines,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                color = containerColor,
                shape = RoundedCornerShape(10.dp),
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
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                )
            }
        }
    }
}
