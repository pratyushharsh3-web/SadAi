package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskItem
import com.example.ui.SadbhavnaViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonSky
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleScreen(
    viewModel: SadbhavnaViewModel,
    tasks: List<TaskItem>,
    modifier: Modifier = Modifier
) {
    var filterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Today, 2: Pending
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredTasks = when (filterTab) {
        1 -> tasks.filter {
            val taskCal = Calendar.getInstance().apply { timeInMillis = it.dueTimestamp }
            val nowCal = Calendar.getInstance()
            taskCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) &&
                    taskCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
        }
        2 -> tasks.filter { !it.isCompleted }
        else -> tasks
    }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Schedule & Tasks",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${tasks.count { !it.isCompleted }} pending reminders active",
                        color = ElectricCyan,
                        fontSize = 12.sp
                    )
                }

                // Add Task Icon Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan)
                        .clickable { showAddDialog = true }
                        .testTag("add_task_header_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task",
                        tint = Color(0xFF04101E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTabItem(
                    title = "All (${tasks.size})",
                    isSelected = filterTab == 0,
                    onClick = { filterTab = 0 }
                )
                FilterTabItem(
                    title = "Today",
                    isSelected = filterTab == 1,
                    onClick = { filterTab = 1 }
                )
                FilterTabItem(
                    title = "Pending (${tasks.count { !it.isCompleted }})",
                    isSelected = filterTab == 2,
                    onClick = { filterTab = 2 }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Koi task ya reminder nahi mila.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aap bol sakte hain: \"Kal subah 6 baje reminder lagao\"",
                            color = ElectricCyan,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCardItem(
                            task = task,
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }

        // Add Task Dialog
        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, desc, cat, timeMillis, hasReminder ->
                    viewModel.addTask(title, desc, cat, timeMillis, hasReminder)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun FilterTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) ElectricCyan else DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) Color(0xFF04101E) else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun TaskCardItem(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (task.category.lowercase()) {
        "study" -> Color(0xFF8B5CF6)
        "call" -> Color(0xFF10B981)
        "routine" -> Color(0xFFF59E0B)
        else -> ElectricCyan
    }

    val timeFormatted = SimpleDateFormat("EEE, d MMM • hh:mm a", Locale.getDefault()).format(Date(task.dueTimestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (task.isCompleted) Color(0xFF162540) else Color(0xFF1E2E4E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle completion",
                    tint = if (task.isCompleted) Color(0xFF10B981) else TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) TextSecondary else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.category,
                            color = categoryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeFormatted,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, desc: String, category: String, dueTimestamp: Long, hasReminder: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Study") }
    var hasReminder by remember { mutableStateOf(true) }
    var offsetHours by remember { mutableIntStateOf(1) }

    val categories = listOf("Study", "Routine", "Call", "Work", "General")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Naya Reminder / Task Banayein",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title (शीर्षक)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = Color(0xFF1E355B),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (विवरण - Optional)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = Color(0xFF1E355B),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Category chunein:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (category == cat) ElectricCyan else DarkSurfaceVariant)
                                .clickable { category = cat }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (category == cat) Color(0xFF04101E) else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Samay (Time Offset):", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1 to "1 Hour", 3 to "3 Hours", 6 to "6 Hours", 12 to "Tomorrow").forEach { (h, label) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (offsetHours == h) NeonSky else DarkSurfaceVariant)
                                .clickable { offsetHours = h }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (offsetHours == h) Color(0xFF04101E) else TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.HOUR_OF_DAY, offsetHours)
                        onAdd(title.trim(), desc.trim(), category, cal.timeInMillis, hasReminder)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                modifier = Modifier.testTag("submit_new_task_button")
            ) {
                Text("Schedule Karein", color = Color(0xFF04101E), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
