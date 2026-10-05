package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.CategoryEntity
import com.example.data.CountdownEntity
import com.example.model.CategoryDef
import com.example.model.CountdownCategories
import com.example.model.TimeZoneHelper
import com.example.ui.components.CategoryEditDialog
import com.example.ui.components.CategoryCountItem
import com.example.ui.components.CreateCustomAlertDialog
import com.example.ui.components.ManageCategoriesSheet
import com.example.ui.components.TimeZonePickerSheet
import com.example.ui.viewmodel.CountdownViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditCountdownScreen(
    timerId: Long = 0,
    viewModel: CountdownViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    val context = LocalContext.current
    val allTimers by viewModel.allTimers.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()
    val existing = allTimers.find { it.id == timerId }

    val initialZoneId = existing?.timeZoneId ?: TimeZoneHelper.getLocalZoneId()
    val initialZdt = if (existing != null) {
        Instant.ofEpochMilli(existing.targetEpochMillis).atZone(ZoneId.of(initialZoneId))
    } else {
        ZonedDateTime.now(ZoneId.of(initialZoneId)).plusDays(7).withHour(12).withMinute(0).withSecond(0)
    }

    val categoryCounts by viewModel.categoryCounts.collectAsState()

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(existing?.category ?: "Milestone") }
    var selectedZoneId by remember { mutableStateOf(initialZoneId) }
    var selectedDate by remember { mutableStateOf(initialZdt.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(initialZdt.toLocalTime()) }
    var notifyOnFinish by remember { mutableStateOf(existing?.notifyOnFinish ?: true) }
    val defaultPresetAlerts = remember { listOf(0, 15, 60, 1440, 10080) }
    var selectedAlertMinutes by remember {
        val initial = existing?.getAlertMinutes() ?: listOf(0)
        mutableStateOf(if (initial.isEmpty()) setOf(0) else initial.toSet())
    }
    var availableAlerts by remember {
        val existingAlerts = existing?.getAlertMinutes() ?: emptyList()
        mutableStateOf((defaultPresetAlerts + existingAlerts).distinct().sorted())
    }
    var showCustomAlertDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    var titleError by remember { mutableStateOf(false) }
    var showTimeZoneSheet by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showManageCategoriesSheet by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }

    val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault()) }
    val timeFormatter = remember(is24Hour) {
        val pattern = if (is24Hour) "HH:mm" else "h:mm a"
        DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (timerId == 0L) "New Countdown" else "Edit Countdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title Input
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Event Title *") },
                    placeholder = { Text("e.g. Vacation in Hawaii, Graduation Day") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) Text("Please enter a title for your countdown")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_countdown_title")
                )
            }

            // Category Section with Manage & Edit capability
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showManageCategoriesSheet = true },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("button_manage_categories")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manage", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    categoryToEdit = null
                                    showAddCategoryDialog = true
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("button_open_add_category_dialog"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Category",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "New",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                val allDisplayCategories = if (customCategories.isNotEmpty()) {
                    customCategories.map {
                        CategoryDef(
                            name = it.name,
                            icon = CountdownCategories.getIconByName(it.iconName),
                            colorHex = it.colorHex,
                            iconName = it.iconName
                        )
                    }
                } else {
                    CountdownCategories.DEFAULT_LIST
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allDisplayCategories.forEach { cat ->
                        val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                        val catColor = Color(cat.colorHex)
                        val buttonShape = RoundedCornerShape(14.dp)

                        Surface(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(buttonShape)
                                .clickable { selectedCategory = cat.name }
                                .testTag("category_chip_select_${cat.name.lowercase()}"),
                            shape = buttonShape,
                            color = if (isSelected) catColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable {
                                                val found = customCategories.find { it.name.equals(cat.name, ignoreCase = true) }
                                                categoryToEdit = found ?: CategoryEntity(
                                                    name = cat.name,
                                                    iconName = cat.iconName,
                                                    colorHex = cat.colorHex
                                                )
                                                showAddCategoryDialog = true
                                            }
                                            .padding(3.dp)
                                            .testTag("button_edit_category_${cat.name.lowercase()}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Category",
                                            tint = catColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Date and Time Pickers
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Target Date & Time",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date Button
                    Surface(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                    },
                                    selectedDate.year,
                                    selectedDate.monthValue - 1,
                                    selectedDate.dayOfMonth
                                ).show()
                            }
                            .testTag("button_pick_date"),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Date",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedDate.format(dateFormatter),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }

                    // Time Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        selectedTime = LocalTime.of(hourOfDay, minute)
                                    },
                                    selectedTime.hour,
                                    selectedTime.minute,
                                    is24Hour
                                ).show()
                            }
                            .testTag("button_pick_time"),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedTime.format(timeFormatter),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }

            // Time Zone Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Time Zone",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { showTimeZoneSheet = true }
                        .testTag("button_select_timezone"),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = TimeZoneHelper.formatZoneSummary(selectedZoneId),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Tap to switch to any world time zone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Notification & Alerts Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Alert Notifications",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Chime and haptic vibration alert",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = notifyOnFinish,
                            onCheckedChange = { notifyOnFinish = it },
                            modifier = Modifier.testTag("switch_notification")
                        )
                    }

                    if (notifyOnFinish) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reminder Notices (Select Multiple)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${selectedAlertMinutes.size} active",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val buttonShape = RoundedCornerShape(14.dp)
                            availableAlerts.forEach { minutes ->
                                val isSelected = selectedAlertMinutes.contains(minutes)
                                val isPreset = defaultPresetAlerts.contains(minutes)

                                Surface(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clip(buttonShape)
                                        .clickable {
                                            selectedAlertMinutes = if (isSelected) {
                                                selectedAlertMinutes - minutes
                                            } else {
                                                selectedAlertMinutes + minutes
                                            }
                                        }
                                        .testTag("chip_alert_notice_$minutes"),
                                    shape = buttonShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsNone,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Text(
                                            text = CountdownEntity.formatAlertOffsetLabel(minutes),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )

                                        if (!isPreset) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        availableAlerts = availableAlerts - minutes
                                                        selectedAlertMinutes = selectedAlertMinutes - minutes
                                                    }
                                                    .padding(3.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove Custom Alert",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Button to create custom alert notification with matching button size
                            Surface(
                                modifier = Modifier
                                    .height(48.dp)
                                    .clip(buttonShape)
                                    .clickable { showCustomAlertDialog = true }
                                    .testTag("button_add_custom_alert"),
                                shape = buttonShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Custom Alert",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Custom Alert...",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes & Details (Optional)") },
                placeholder = { Text("Location, flight confirmation, reminders, or goals") },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_countdown_notes")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }

                    // Compute epoch millis with chosen timezone
                    val targetZdt = ZonedDateTime.of(
                        selectedDate,
                        selectedTime,
                        ZoneId.of(selectedZoneId)
                    )

                    val alertsListStr = selectedAlertMinutes.sorted().joinToString(",")
                    viewModel.saveTimer(
                        existingId = timerId,
                        title = title,
                        targetEpochMillis = targetZdt.toInstant().toEpochMilli(),
                        timeZoneId = selectedZoneId,
                        category = selectedCategory,
                        colorIndex = 0,
                        iconName = selectedCategory,
                        notifyOnFinish = notifyOnFinish && selectedAlertMinutes.isNotEmpty(),
                        notifyAdvanceMinutes = selectedAlertMinutes.minOrNull() ?: 0,
                        alertMinutesList = alertsListStr,
                        notes = notes
                    )

                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("button_save_countdown"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (timerId == 0L) "Create Countdown" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showTimeZoneSheet) {
        TimeZonePickerSheet(
            selectedZoneId = selectedZoneId,
            onZoneSelected = { selectedZoneId = it },
            onDismiss = { showTimeZoneSheet = false }
        )
    }

    if (showManageCategoriesSheet) {
        ManageCategoriesSheet(
            categories = customCategories,
            categoryCounts = categoryCounts,
            onDismiss = { showManageCategoriesSheet = false },
            onEditCategory = { cat ->
                showManageCategoriesSheet = false
                categoryToEdit = cat
                showAddCategoryDialog = true
            },
            onAddNewCategory = {
                showManageCategoriesSheet = false
                categoryToEdit = null
                showAddCategoryDialog = true
            }
        )
    }

    if (showAddCategoryDialog) {
        val existingNames = customCategories.map { it.name }
        CategoryEditDialog(
            categoryToEdit = categoryToEdit,
            onDismissRequest = {
                showAddCategoryDialog = false
                categoryToEdit = null
            },
            onSaveCategory = { name, iconName, colorHex ->
                val targetCat = categoryToEdit
                if (targetCat != null) {
                    viewModel.updateCategory(
                        oldName = targetCat.name,
                        category = targetCat.copy(
                            name = name,
                            iconName = iconName,
                            colorHex = colorHex
                        )
                    )
                    if (selectedCategory.equals(targetCat.name, ignoreCase = true)) {
                        selectedCategory = name
                    }
                } else {
                    viewModel.addCategory(name, iconName, colorHex)
                    selectedCategory = name
                }
            },
            onDeleteCategory = if (categoryToEdit != null) {
                {
                    val toDelete = categoryToEdit!!
                    viewModel.deleteCategory(toDelete)
                    if (selectedCategory.equals(toDelete.name, ignoreCase = true)) {
                        selectedCategory = customCategories.firstOrNull { it.id != toDelete.id }?.name ?: "Personal"
                    }
                }
            } else null,
            existingCategoryNames = existingNames
        )
    }

    if (showCustomAlertDialog) {
        CreateCustomAlertDialog(
            onDismissRequest = { showCustomAlertDialog = false },
            onAlertCreated = { newMinutes ->
                availableAlerts = (availableAlerts + newMinutes).distinct().sorted()
                selectedAlertMinutes = selectedAlertMinutes + newMinutes
            }
        )
    }
}
