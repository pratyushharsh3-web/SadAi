package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.core.content.ContextCompat
import com.example.ui.AppTab
import com.example.ui.SadbhavnaViewModel
import com.example.ui.components.ConfirmationDialogView
import com.example.ui.components.ContactSelectionDialogView
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PhoneToolsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonSky
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: SadbhavnaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: SadbhavnaViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val assistantState by viewModel.assistantState.collectAsState()
    val liveTranscription by viewModel.liveTranscription.collectAsState()
    val todayTasks by viewModel.todayTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val statusBanner by viewModel.statusBanner.collectAsState()
    val confirmationDialog by viewModel.confirmationDialog.collectAsState()
    val contactSelectionDialog by viewModel.contactSelectionDialog.collectAsState()

    var hasMicPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasMicPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Handle Android system Back Button
    BackHandler(enabled = activeTab != AppTab.DASHBOARD) {
        viewModel.setActiveTab(AppTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = DarkBackground,
        bottomBar = {
            SadbhavnaBottomBar(
                currentTab = activeTab,
                onTabSelect = { viewModel.setActiveTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            // Screen Switcher
            when (activeTab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    assistantState = assistantState,
                    liveTranscription = liveTranscription,
                    todayTasks = todayTasks
                )
                AppTab.CHAT -> ChatScreen(
                    viewModel = viewModel,
                    messages = chatMessages,
                    assistantState = assistantState
                )
                AppTab.SCHEDULE -> ScheduleScreen(
                    viewModel = viewModel,
                    tasks = allTasks
                )
                AppTab.TOOLS -> PhoneToolsScreen(
                    viewModel = viewModel
                )
                AppTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }

            // Top Status / Notification Banner
            AnimatedVisibility(
                visible = statusBanner != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                statusBanner?.let { message ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0C1F3D))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = message,
                                color = ElectricCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { viewModel.clearBanner() }
                            )
                        }
                    }
                }
            }

            // Confirmation and Contact Selection Dialogs
            confirmationDialog?.let { data ->
                ConfirmationDialogView(data)
            }

            contactSelectionDialog?.let { data ->
                ContactSelectionDialogView(data)
            }
        }
    }
}

@Composable
fun SadbhavnaBottomBar(
    currentTab: AppTab,
    onTabSelect: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("main_navigation_bar")
    ) {
        NavigationBarItem(
            selected = currentTab == AppTab.DASHBOARD,
            onClick = { onTabSelect(AppTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF04101E),
                selectedTextColor = ElectricCyan,
                indicatorColor = ElectricCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_dashboard")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.CHAT,
            onClick = { onTabSelect(AppTab.CHAT) },
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat") },
            label = { Text("Chat", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF04101E),
                selectedTextColor = ElectricCyan,
                indicatorColor = ElectricCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_chat")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.SCHEDULE,
            onClick = { onTabSelect(AppTab.SCHEDULE) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Schedule") },
            label = { Text("Tasks", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF04101E),
                selectedTextColor = ElectricCyan,
                indicatorColor = ElectricCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_schedule")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.TOOLS,
            onClick = { onTabSelect(AppTab.TOOLS) },
            icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = "Tools") },
            label = { Text("Tools", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF04101E),
                selectedTextColor = ElectricCyan,
                indicatorColor = ElectricCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_tools")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.SETTINGS,
            onClick = { onTabSelect(AppTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF04101E),
                selectedTextColor = ElectricCyan,
                indicatorColor = ElectricCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_settings")
        )
    }
}
