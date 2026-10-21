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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.ui.SadbhavnaViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonSky
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PhoneToolsScreen(
    viewModel: SadbhavnaViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var contactQuery by remember { mutableStateOf("") }
    var smsNumber by remember { mutableStateOf("") }
    var smsBody by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Android Phone Control Engine",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Authorized native actions, applications, settings & calls",
            color = ElectricCyan,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Applications Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Launch Supported Applications",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppShortcutTile("YouTube", Icons.Default.PlayArrow, Color(0xFFFF4444)) {
                        viewModel.triggerAppAction("YOUTUBE")
                    }
                    AppShortcutTile("Camera", Icons.Default.CameraAlt, Color(0xFF00E5FF)) {
                        viewModel.triggerAppAction("CAMERA")
                    }
                    AppShortcutTile("Calculator", Icons.Default.Calculate, Color(0xFF10B981)) {
                        viewModel.triggerAppAction("CALCULATOR")
                    }
                    AppShortcutTile("Maps", Icons.Default.Map, Color(0xFF38BDF8)) {
                        viewModel.triggerAppAction("MAPS")
                    }
                    AppShortcutTile("Settings", Icons.Default.Settings, Color(0xFFF59E0B)) {
                        viewModel.triggerAppAction("SETTINGS")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Settings Shortcuts
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "System Settings (Direct Shortcuts)",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SettingTile("Wi-Fi", Icons.Default.Wifi, Color(0xFF00E5FF)) {
                        viewModel.triggerAppAction("WIFI")
                    }
                    SettingTile("Bluetooth", Icons.Default.Bluetooth, Color(0xFF38BDF8)) {
                        viewModel.triggerAppAction("BLUETOOTH")
                    }
                    SettingTile("Display", Icons.Default.Brightness6, Color(0xFFF59E0B)) {
                        viewModel.processCommand("display settings")
                    }
                    SettingTile("Battery", Icons.Default.BatterySaver, Color(0xFF10B981)) {
                        viewModel.processCommand("battery settings")
                    }
                    SettingTile("Location", Icons.Default.LocationOn, Color(0xFFEF4444)) {
                        viewModel.processCommand("location settings")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Audio & Volume Control
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Device Volume Controls",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.processCommand("volume kam karo") },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.VolumeDown, contentDescription = "Volume Down", tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Volume -", color = TextPrimary)
                    }

                    Button(
                        onClick = { viewModel.processCommand("volume badhao") },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Volume Up", tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Volume +", color = TextPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Voice Contact Calling Workflow
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Call Contact (कॉल लगाएं)",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aap aawaz se keh sakte hain: \"Papa ko call karo\" ya yahan naam likhein:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = contactQuery,
                        onValueChange = { contactQuery = it },
                        placeholder = { Text("Contact name / Number", color = TextSecondary, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Color(0xFF1E355B),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (contactQuery.isNotBlank()) {
                                viewModel.processCommand("$contactQuery ko call karo")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("call_contact_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF04101E))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. SMS & Message Drafter
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sms, contentDescription = null, tint = NeonSky, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SMS & Messaging Draft Tool",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = smsNumber,
                    onValueChange = { smsNumber = it },
                    placeholder = { Text("Recipient Name or Phone Number", color = TextSecondary, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = Color(0xFF1E355B),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = smsBody,
                    onValueChange = { smsBody = it },
                    placeholder = { Text("Message Body (Sandesh)", color = TextSecondary, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = Color(0xFF1E355B),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (smsNumber.isNotBlank()) {
                            val msg = smsBody.ifBlank { "Namaste! Sadbhavna AI dwara sandesh." }
                            viewModel.processCommand("$smsNumber ko message likho ki $msg")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonSky),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("draft_sms_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF04101E))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Prepare SMS Draft (समीक्षा करें)", color = Color(0xFF04101E), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AppShortcutTile(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = name, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
fun SettingTile(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
                .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = name, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, color = TextSecondary, fontSize = 11.sp)
    }
}
