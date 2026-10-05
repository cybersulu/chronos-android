package com.example.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.CategoryEntity
import com.example.data.CountdownEntity
import com.example.model.CountdownBreakdown
import com.example.model.CountdownCategories
import com.example.ui.components.CategoryEditDialog
import com.example.ui.components.CategoryFilterBar
import com.example.ui.components.FullCountdownGrid
import com.example.ui.components.ManageCategoriesSheet
import com.example.ui.theme.TimerColorPalette
import com.example.ui.viewmodel.CountdownViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownListScreen(
    viewModel: CountdownViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val filteredTimers by viewModel.filteredTimers.collectAsState()
    val allTimers by viewModel.allTimers.collectAsState()
    val categoryCounts by viewModel.categoryCounts.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    // Observe ticker so all items recompose smoothly every second
    val tickerTime by viewModel.currentTickerTime.collectAsState()

    val context = LocalContext.current

    // Drag-and-drop state
    var draggedItemIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showManageCategoriesSheet by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }

    fun triggerDragHaptic() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "CHRONOS",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${allTimers.size} active countdowns",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("fab_add_countdown")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Timer")
                    Text(text = "New Event", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Pills with distinct counters and edit/manage triggers
            CategoryFilterBar(
                categoryCounts = categoryCounts,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                onAddCategoryClick = {
                    categoryToEdit = null
                    showAddCategoryDialog = true
                },
                onManageCategoriesClick = {
                    showManageCategoriesSheet = true
                },
                onEditSelectedCategory = { catName ->
                    val found = customCategories.find { it.name.equals(catName, ignoreCase = true) }
                    categoryToEdit = found ?: CategoryEntity(
                        name = catName,
                        iconName = catName,
                        colorHex = CountdownCategories.getColorHexForCategory(catName)
                    )
                    showAddCategoryDialog = true
                }
            )

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
                        } else {
                            viewModel.addCategory(name, iconName, colorHex)
                            viewModel.selectCategory(name)
                        }
                    },
                    onDeleteCategory = if (categoryToEdit != null) {
                        {
                            viewModel.deleteCategory(categoryToEdit!!)
                        }
                    } else null,
                    existingCategoryNames = categoryCounts.map { it.name }
                )
            }

            if (filteredTimers.isEmpty()) {
                EmptyStateView(
                    category = selectedCategory,
                    onAddNew = onNavigateToAdd
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = filteredTimers,
                        key = { _, item -> item.id }
                    ) { index, timer ->
                        val isBeingDragged = draggedItemIndex == index

                        // Use the default Accent Color set for the timer's category
                        val itemColor = Color(CountdownCategories.getColorHexForCategory(timer.category))
                        val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }

                        // Recompute with current ticker
                        val breakdown = remember(timer.targetEpochMillis, timer.timeZoneId, tickerTime, is24Hour) {
                            CountdownBreakdown.compute(timer.targetEpochMillis, timer.timeZoneId, is24Hour)
                        }

                        CountdownCard(
                            timer = timer,
                            breakdown = breakdown,
                            accentColor = itemColor,
                            isBeingDragged = isBeingDragged,
                            dragOffsetY = if (isBeingDragged) dragOffsetY else 0f,
                            onCardClick = { onNavigateToDetail(timer.id) },
                            onEdit = { onNavigateToEdit(timer.id) },
                            onDelete = { viewModel.deleteTimer(timer) },
                            onMoveUp = { viewModel.moveTimerUp(timer) },
                            onMoveDown = { viewModel.moveTimerDown(timer) },
                            canMoveUp = index > 0,
                            canMoveDown = index < filteredTimers.size - 1,
                            dragModifier = Modifier.pointerInput(timer.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedItemIndex = index
                                        dragOffsetY = 0f
                                        triggerDragHaptic()
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y

                                        val threshold = 160f
                                        if (dragOffsetY > threshold && draggedItemIndex < filteredTimers.size - 1) {
                                            viewModel.moveTimer(draggedItemIndex, draggedItemIndex + 1)
                                            draggedItemIndex += 1
                                            dragOffsetY -= threshold
                                            triggerDragHaptic()
                                        } else if (dragOffsetY < -threshold && draggedItemIndex > 0) {
                                            viewModel.moveTimer(draggedItemIndex, draggedItemIndex - 1)
                                            draggedItemIndex -= 1
                                            dragOffsetY += threshold
                                            triggerDragHaptic()
                                        }
                                    },
                                    onDragEnd = {
                                        draggedItemIndex = -1
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        draggedItemIndex = -1
                                        dragOffsetY = 0f
                                    }
                                )
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownCard(
    timer: CountdownEntity,
    breakdown: CountdownBreakdown,
    accentColor: Color,
    isBeingDragged: Boolean,
    dragOffsetY: Float,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    dragModifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, dragOffsetY.roundToInt()) }
            .zIndex(if (isBeingDragged) 10f else 1f)
            .shadow(
                elevation = if (isBeingDragged) 16.dp else 4.dp,
                shape = cardShape,
                ambientColor = accentColor.copy(alpha = 0.4f),
                spotColor = accentColor
            )
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = 0.4f),
                        accentColor.copy(alpha = 0.1f)
                    )
                ),
                shape = cardShape
            )
            .clickable(onClick = onCardClick)
            .padding(18.dp)
            .testTag("countdown_card_${timer.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row: Category tag, Drag handle & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = CountdownCategories.getIconForCategory(timer.category),
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = timer.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = accentColor
                            )
                        }
                    }
                }

                // Drag handle and overflow menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Long press drag handle
                    Box(
                        modifier = dragModifier
                            .clip(CircleShape)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Drag to reorder",
                            tint = if (isBeingDragged) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Overflow Menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            if (canMoveUp) {
                                DropdownMenuItem(
                                    text = { Text("Move Up") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null)
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onMoveUp()
                                    }
                                )
                            }
                            if (canMoveDown) {
                                DropdownMenuItem(
                                    text = { Text("Move Down") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null)
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onMoveDown()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = timer.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Date & Timezone string
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${breakdown.formattedTargetDate} at ${breakdown.formattedTargetTime} • ${breakdown.timeZoneDisplayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6-Unit Countdown Grid (Years, Months, Days, Hours, Minutes, Seconds)
            FullCountdownGrid(
                breakdown = breakdown,
                accentColor = accentColor,
                isLarge = false
            )

            if (breakdown.isPast) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.14f))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Event has arrived! (Counting elapsed time)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    category: String,
    onAddNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.HourglassBottom,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (category == CountdownCategories.ALL) "No Countdowns Yet" else "No $category Countdowns",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create an event to start tracking years, months, days, hours, and seconds.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        androidx.compose.material3.Button(
            onClick = onAddNew,
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create Countdown")
        }
    }
}
