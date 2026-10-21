package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskItem
import com.example.speech.AssistantState
import com.example.ui.AppTab
import com.example.ui.SadbhavnaViewModel
import com.example.ui.components.HeaderStatusCard
import com.example.ui.components.VisualizerOrb
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonSky
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: SadbhavnaViewModel,
    assistantState: AssistantState,
    liveTranscription: String,
    todayTasks: List<TaskItem>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Status Header
        HeaderStatusCard(
            batteryInfo = viewModel.batteryInfo.value,
            isOnline = viewModel.isOnline.value,
            currentLanguage = viewModel.language.value,
            onLanguageToggle = {
                val next = if (viewModel.language.value.startsWith("hi")) "en-IN" else "hi-IN"
                viewModel.setLanguage(next)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Center Futuristic Animated Orb
        VisualizerOrb(
            state = assistantState,
            onClick = {
                if (assistantState == AssistantState.SPEAKING) {
                    viewModel.stopSpeaking()
                } else {
                    viewModel.toggleListening()
                }
            }
        )

        // Stop speaking button if currently speaking
        if (assistantState == AssistantState.SPEAKING) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { viewModel.stopSpeaking() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("stop_speaking_button")
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stop Speaking (आवाज रोकें)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Live transcription bubble
        AnimatedVisibility(
            visible = liveTranscription.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "Aapne bola:",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"$liveTranscription\"",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Spoken Command Suggestions
        Text(
            text = "Suggested Voice Commands (आवाज से कहें):",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        val suggestions = listOf(
            "Jarvis, YouTube kholo",
            "Jarvis, battery kitni hai?",
            "Kitne baje hain?",
            "Papa ko call karo",
            "WiFi settings kholo",
            "Mere tasks batao",
            "Calculator open karo",
            "Kal subah 6 baje reminder lagao"
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, Color(0xFF1E355B), RoundedCornerShape(20.dp))
                        .clickable { viewModel.processCommand(suggestion) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "💬 $suggestion",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Today's Schedule Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = NeonSky,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Today's Schedule & Reminders",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = "View All",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { viewModel.setActiveTab(AppTab.SCHEDULE) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val pendingTasks = todayTasks.filter { !it.isCompleted }
                if (pendingTasks.isEmpty()) {
                    Text(
                        text = "Aaj koi pending task nahi hai. Ekdum chill karein! 🌟",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    pendingTasks.take(2).forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleTask(task) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Toggle",
                                    tint = if (task.isCompleted) Color(0xFF10B981) else TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(task.dueTimestamp))
                                Text(
                                    text = "$timeStr • ${task.category}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Phone Launchers Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Quick Phone Tools (फोन टूल्स)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    QuickToolItem(
                        icon = Icons.Default.PlayArrow,
                        title = "YouTube",
                        color = Color(0xFFFF4444),
                        onClick = { viewModel.triggerAppAction("YOUTUBE") }
                    )
                    QuickToolItem(
                        icon = Icons.Default.CameraAlt,
                        title = "Camera",
                        color = Color(0xFF00E5FF),
                        onClick = { viewModel.triggerAppAction("CAMERA") }
                    )
                    QuickToolItem(
                        icon = Icons.Default.Calculate,
                        title = "Calculator",
                        color = Color(0xFF10B981),
                        onClick = { viewModel.triggerAppAction("CALCULATOR") }
                    )
                    QuickToolItem(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        color = Color(0xFFF59E0B),
                        onClick = { viewModel.triggerAppAction("SETTINGS") }
                    )
                    QuickToolItem(
                        icon = Icons.Default.Map,
                        title = "Maps",
                        color = Color(0xFF38BDF8),
                        onClick = { viewModel.triggerAppAction("MAPS") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun QuickToolItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
