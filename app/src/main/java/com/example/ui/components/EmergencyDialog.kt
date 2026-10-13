package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TriageOption
import com.example.ui.theme.BorderEmergencyRed
import com.example.ui.theme.DeepEmergencyRed
import com.example.ui.theme.DeepTextBody
import com.example.ui.theme.DeepTextHeadlines
import com.example.ui.theme.LightEmergencyContainer
import com.example.ui.theme.MutedTextSlate
import com.example.ui.theme.OnLightEmergencyContainer
import com.example.ui.theme.PureWhiteSurface

@Composable
fun EmergencyDialog(
    option: TriageOption,
    onAcknowledgeAndProceed: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = PureWhiteSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, BorderEmergencyRed),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("emergency_alert_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emergency Alert Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(LightEmergencyContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Emergency Alert",
                        tint = DeepEmergencyRed,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "CRITICAL RED-FLAG ALERT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = DeepEmergencyRed,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = option.text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = DeepTextHeadlines,
                    textAlign = TextAlign.Center
                )

                if (!option.subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = option.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedTextSlate,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = LightEmergencyContainer,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = "This symptom indicates high risk of an acute medical emergency. Immediate clinical evaluation or emergency services is advised.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnLightEmergencyContainer,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                        context.startActivity(dialIntent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepEmergencyRed,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("call_emergency_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call 911"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Call Emergency (911 / 112)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onAcknowledgeAndProceed,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, DeepEmergencyRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepEmergencyRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("view_emergency_protocol_button")
                ) {
                    Text(
                        text = "View Clinical Care Instructions",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_emergency_dialog_button")
                ) {
                    Text(
                        text = "Return to Questionnaire",
                        color = MutedTextSlate
                    )
                }
            }
        }
    }
}
