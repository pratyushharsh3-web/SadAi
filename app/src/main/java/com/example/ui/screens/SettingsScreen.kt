package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.SadbhavnaViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonSky
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: SadbhavnaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val currentMode by viewModel.aiMode.collectAsState()
    val currentLang by viewModel.language.collectAsState()
    val currentRate by viewModel.speechRate.collectAsState()
    val currentPitch by viewModel.speechPitch.collectAsState()

    var sliderRate by remember(currentRate) { mutableFloatStateOf(currentRate) }
    var sliderPitch by remember(currentPitch) { mutableFloatStateOf(currentPitch) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Sadbhavna AI Settings",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Voice preferences, AI brain, privacy & device setup",
            color = ElectricCyan,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. AI Operation Mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (currentMode == "offline") Icons.Default.CloudOff else Icons.Default.Cloud,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Operation Mode",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sadbhavna AI can function completely offline for phone actions, or combine with Gemini 3.5 Flash for educational reasoning.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "hybrid" to "Hybrid (Smartest)",
                        "offline" to "Offline Only",
                        "online" to "Online Brain"
                    ).forEach { (mode, label) ->
                        val isSelected = currentMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricCyan else DarkSurfaceVariant)
                                .clickable { viewModel.setAiMode(mode) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color(0xFF04101E) else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Language Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = NeonSky, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assistant Voice Language",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isHindi = currentLang.startsWith("hi")
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isHindi) ElectricCyan else DarkSurfaceVariant)
                            .clickable { viewModel.setLanguage("hi-IN") }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🇮🇳 हिन्दी / Hinglish",
                            color = if (isHindi) Color(0xFF04101E) else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isHindi) ElectricCyan else DarkSurfaceVariant)
                            .clickable { viewModel.setLanguage("en-IN") }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌐 English (India)",
                            color = if (!isHindi) Color(0xFF04101E) else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Speech Rate & Pitch Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice Speed & Pitch",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text("Speech Rate (Speed): ${String.format("%.1fx", sliderRate)}", color = TextSecondary, fontSize = 12.sp)
                Slider(
                    value = sliderRate,
                    onValueChange = {
                        sliderRate = it
                        viewModel.setSpeechSettings(sliderRate, sliderPitch)
                    },
                    valueRange = 0.6f..1.6f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text("Speech Pitch: ${String.format("%.1fx", sliderPitch)}", color = TextSecondary, fontSize = 12.sp)
                Slider(
                    value = sliderPitch,
                    onValueChange = {
                        sliderPitch = it
                        viewModel.setSpeechSettings(sliderRate, sliderPitch)
                    },
                    valueRange = 0.6f..1.5f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonSky,
                        activeTrackColor = NeonSky,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.speakText("Ji namaste! Main Jarvis hoon. Aap bataiye, main aapki kaise help kar sakti hoon?")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Assistant Voice (आवाज सुनें)", color = TextPrimary, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Privacy & Permissions Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy & Local Device Integrity",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• All tasks, reminders, and history are stored 100% locally in on-device Room SQLite database.\n• No background mic recording when inactive.\n• Safe calling & messaging require explicit confirmation.\n• Zero telemetry tracking or third-party data selling.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Manage Android Permissions in OS Settings", color = ElectricCyan, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Mobile-Only Android Developer & APK Guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Phone/Tablet Development Guide",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aap bina kisi computer/laptop ke is application ko install aur use kar sakte hain:\n" +
                            "1. Top-right settings menu se 'Export Project as ZIP' ya 'Build APK' download karein.\n" +
                            "2. Apne Android phone ke File Manager mein downloaded APK par tap karke 'Install' karein.\n" +
                            "3. First launch par Microphone aur Notification permission allow karein.\n" +
                            "4. Offline reh kar bhi 'YouTube kholo', 'Battery kitni hai' aadi sabhi commands kaam karenge!",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. About Sadbhavna AI
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "About Sadbhavna AI",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"Samjhe bhi, saath bhi de.\"",
                    color = ElectricCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Version 1.0.0 • Package: com.sadbhavna.ai\nPowered by Jetpack Compose, Android Speech Recognition, Room SQLite, and optional Gemini 3.5 Flash intelligence.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
