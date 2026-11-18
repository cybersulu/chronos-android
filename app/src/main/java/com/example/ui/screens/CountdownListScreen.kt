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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SortByAlpha
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.CategoryEntity
import com.example.data.CountdownEntity
import com.example.model.CountdownBreakdown
import com.example.model.CountdownCategories
import com.example.model.CountdownSortOption
import com.example.ui.components.CategoryEditDialog
import com.example.ui.components.CategoryFilterBar
import com.example.ui.components.SingleLineCountdownRow
import com.example.ui.components.ManageCategoriesSheet
import com.example.ui.viewmodel.CountdownViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.max
import kotlin.math.min

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
    val sortOption by viewModel.sortOption.collectAsState()
    // Observe ticker so all items recompose smoothly every second
    val tickerTime by viewModel.currentTickerTime.collectAsState()

    val context = LocalContext.current
    val density = LocalDensity.current
    val listState = rememberLazyListState()

    val itemSpacingDp = 16.dp
    val itemSpacingPx = with(density) { itemSpacingDp.toPx() }
    val autoScrollZonePx = with(density) { 88.dp.toPx() }
    val maxAutoScrollSpeedPxPerSec = with(density) { 780.dp.toPx() }
    val minAutoScrollSpeedPxPerSec = with(density) { 140.dp.toPx() }
    val edgeDragTriggerPx = with(density) { 8.dp.toPx() }

    // Drag-and-drop state (optimistic local list during drag to avoid Room Flow races)
    var localTimers by remember { mutableStateOf(filteredTimers) }
    var pendingPersistedIds by remember { mutableStateOf<List<Long>?>(null) }
    var draggedItemId by remember { mutableStateOf<Long?>(null) }
    var draggedDistance by remember { mutableFloatStateOf(0f) }
    var pointerYInViewport by remember { mutableFloatStateOf(0f) }
    var minPointerY by remember { mutableFloatStateOf(0f) }
    var maxPointerY by remember { mutableFloatStateOf(0f) }
    var lazyColumnCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showManageCategoriesSheet by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var lastAppliedSort by remember { mutableStateOf(sortOption) }

    val isDragging = draggedItemId != null

    LaunchedEffect(sortOption) {
        if (lastAppliedSort != sortOption) {
            lastAppliedSort = sortOption
            if (localTimers.isNotEmpty() && !isDragging) {
                listState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(filteredTimers, isDragging) {
        if (!isDragging) {
            val pending = pendingPersistedIds
            if (pending == null) {
                localTimers = filteredTimers
            } else if (filteredTimers.map { it.id } == pending || filteredTimers.size != localTimers.size) {
                pendingPersistedIds = null
                localTimers = filteredTimers
            } else {
                // Wait briefly for Room emission to match persisted order, then sync regardless
                delay(400L)
                pendingPersistedIds = null
                localTimers = filteredTimers
            }
        }
    }

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

    fun checkAndSwap() {
        val activeId = draggedItemId ?: return
        val currentIndex = localTimers.indexOfFirst { it.id == activeId }
        if (currentIndex == -1) return

        val visibleItems = listState.layoutInfo.visibleItemsInfo
        val draggedInfo = visibleItems.find { it.key == activeId } ?: return
        // Wait until LazyColumn layout reflects the latest swap before checking again
        if (draggedInfo.index != currentIndex) return

        val visualTop = draggedInfo.offset + draggedDistance
        val visualBottom = visualTop + draggedInfo.size

        if (currentIndex < localTimers.lastIndex) {
            val nextId = localTimers[currentIndex + 1].id
            val nextInfo = visibleItems.find { it.key == nextId }
            if (nextInfo != null) {
                val nextMidpoint = nextInfo.offset + nextInfo.size / 2f
                if (visualBottom > nextMidpoint) {
                    val firstIdx = listState.firstVisibleItemIndex
                    val firstOffset = listState.firstVisibleItemScrollOffset

                    val mutable = localTimers.toMutableList()
                    val moved = mutable.removeAt(currentIndex)
                    mutable.add(currentIndex + 1, moved)
                    localTimers = mutable
                    draggedDistance -= (nextInfo.size + itemSpacingPx)

                    if (currentIndex <= firstIdx || (currentIndex + 1) <= firstIdx) {
                        listState.requestScrollToItem(firstIdx, firstOffset)
                    }
                    triggerDragHaptic()
                    return
                }
            }
        }

        if (currentIndex > 0) {
            val prevId = localTimers[currentIndex - 1].id
            val prevInfo = visibleItems.find { it.key == prevId }
            if (prevInfo != null) {
                val prevMidpoint = prevInfo.offset + prevInfo.size / 2f
                if (visualTop < prevMidpoint) {
                    val firstIdx = listState.firstVisibleItemIndex
                    val firstOffset = listState.firstVisibleItemScrollOffset

                    val mutable = localTimers.toMutableList()
                    val moved = mutable.removeAt(currentIndex)
                    mutable.add(currentIndex - 1, moved)
                    localTimers = mutable
                    draggedDistance += (prevInfo.size + itemSpacingPx)

                    if (currentIndex <= firstIdx || (currentIndex - 1) <= firstIdx) {
                        listState.requestScrollToItem(firstIdx, firstOffset)
                    }
                    triggerDragHaptic()
                }
            }
        }
    }

    // Continuous smooth edge auto-scroll loop while dragging near top or bottom of LazyColumn
    LaunchedEffect(draggedItemId) {
        val activeId = draggedItemId ?: return@LaunchedEffect
        var lastFrameNanos = 0L

        while (isActive && draggedItemId == activeId) {
            val scrollDelta = withFrameNanos { frameNanos ->
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = frameNanos
                    return@withFrameNanos 0f
                }
                val dt = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastFrameNanos = frameNanos

                val layoutInfo = listState.layoutInfo
                val viewportStart = layoutInfo.viewportStartOffset.toFloat()
                val viewportEnd = layoutInfo.viewportEndOffset.toFloat()
                val viewportHeight = viewportEnd - viewportStart
                if (viewportHeight <= 0f) return@withFrameNanos 0f

                val effectiveZone = min(autoScrollZonePx, viewportHeight * 0.22f)
                val pointerY = pointerYInViewport

                when {
                    listState.canScrollBackward &&
                        pointerY < viewportStart + effectiveZone &&
                        pointerY < maxPointerY - edgeDragTriggerPx -> {
                        val depth = ((viewportStart + effectiveZone - pointerY) / effectiveZone).coerceIn(0f, 1.3f)
                        val speed = minAutoScrollSpeedPxPerSec +
                            (maxAutoScrollSpeedPxPerSec - minAutoScrollSpeedPxPerSec) * min(depth, 1f) *
                            max(1f, depth)
                        -speed * dt
                    }
                    listState.canScrollForward &&
                        pointerY > viewportEnd - effectiveZone &&
                        pointerY > minPointerY + edgeDragTriggerPx -> {
                        val depth = ((pointerY - (viewportEnd - effectiveZone)) / effectiveZone).coerceIn(0f, 1.3f)
                        val speed = minAutoScrollSpeedPxPerSec +
                            (maxAutoScrollSpeedPxPerSec - minAutoScrollSpeedPxPerSec) * min(depth, 1f) *
                            max(1f, depth)
                        speed * dt
                    }
                    else -> 0f
                }
            }

            if (scrollDelta != 0f) {
                val consumed = listState.scrollBy(scrollDelta)
                if (consumed != 0f) {
                    draggedDistance += consumed
                    checkAndSwap()
                }
            } else {
                checkAndSwap()
            }
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
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        val isCustom = sortOption == CountdownSortOption.CUSTOM
                        Surface(
                            onClick = { showSortMenu = true },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCustom) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isCustom) {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                }
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("sort_menu_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort countdowns",
                                    tint = if (isCustom) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = sortOption.shortLabel,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isCustom) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            CountdownSortOption.entries.forEach { option ->
                                val isSelected = option == sortOption
                                val optionIcon = when (option) {
                                    CountdownSortOption.CUSTOM -> Icons.Default.DragHandle
                                    CountdownSortOption.CHRONOLOGICAL_ASC -> Icons.Default.ArrowUpward
                                    CountdownSortOption.CHRONOLOGICAL_DESC -> Icons.Default.ArrowDownward
                                    CountdownSortOption.ALPHABETICAL_ASC,
                                    CountdownSortOption.ALPHABETICAL_DESC -> Icons.Default.SortByAlpha
                                }
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = option.menuLabel,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            Text(
                                                text = option.subtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = optionIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    },
                                    trailingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        showSortMenu = false
                                        viewModel.selectSortOption(option)
                                    },
                                    modifier = Modifier.testTag("sort_option_${option.name.lowercase()}")
                                )
                            }
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

            if (localTimers.isEmpty()) {
                EmptyStateView(
                    category = selectedCategory,
                    onAddNew = onNavigateToAdd
                )
            } else {
                LazyColumn(
                    state = listState,
                    userScrollEnabled = !isDragging,
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { lazyColumnCoords = it }
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(itemSpacingDp)
                ) {
                    itemsIndexed(
                        items = localTimers,
                        key = { _, item -> item.id }
                    ) { index, timer ->
                        val isBeingDragged = draggedItemId == timer.id
                        var handleCoords by remember(timer.id) { mutableStateOf<LayoutCoordinates?>(null) }

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
                            dragOffsetY = if (isBeingDragged) draggedDistance else 0f,
                            itemModifier = if (isBeingDragged) {
                                Modifier
                            } else {
                                Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                            },
                            onCardClick = { onNavigateToDetail(timer.id) },
                            onEdit = { onNavigateToEdit(timer.id) },
                            onDelete = { viewModel.deleteTimer(timer) },
                            onMoveUp = { viewModel.moveTimerUp(timer) },
                            onMoveDown = { viewModel.moveTimerDown(timer) },
                            canMoveUp = index > 0,
                            canMoveDown = index < localTimers.size - 1,
                            dragModifier = Modifier
                                .onGloballyPositioned { handleCoords = it }
                                .pointerInput(timer.id) {
                                    fun startDragAt(localStartPos: Offset) {
                                        val lCoords = lazyColumnCoords
                                        val hCoords = handleCoords
                                        val startY = if (lCoords != null && hCoords != null && lCoords.isAttached && hCoords.isAttached) {
                                            lCoords.localPositionOf(hCoords, localStartPos).y
                                        } else {
                                            val info = listState.layoutInfo.visibleItemsInfo.find { it.key == timer.id }
                                            (info?.offset ?: 0).toFloat() + localStartPos.y
                                        }
                                        draggedItemId = timer.id
                                        draggedDistance = 0f
                                        pointerYInViewport = startY
                                        minPointerY = startY
                                        maxPointerY = startY
                                        triggerDragHaptic()
                                    }

                                    fun handleDragStep(localPos: Offset, dragDeltaY: Float) {
                                        draggedDistance += dragDeltaY
                                        val lCoords = lazyColumnCoords
                                        val hCoords = handleCoords
                                        val currentY = if (lCoords != null && hCoords != null && lCoords.isAttached && hCoords.isAttached) {
                                            lCoords.localPositionOf(hCoords, localPos).y
                                        } else {
                                            pointerYInViewport + dragDeltaY
                                        }
                                        pointerYInViewport = currentY
                                        if (currentY < minPointerY) minPointerY = currentY
                                        if (currentY > maxPointerY) maxPointerY = currentY
                                        checkAndSwap()
                                    }

                                    fun finishDrag(commit: Boolean) {
                                        if (commit && localTimers.map { it.id } != filteredTimers.map { it.id }) {
                                            pendingPersistedIds = localTimers.map { it.id }
                                            viewModel.persistTimerOrder(localTimers)
                                        } else if (!commit) {
                                            localTimers = filteredTimers
                                        }
                                        draggedItemId = null
                                        draggedDistance = 0f
                                    }

                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        val longPressTimeout = viewConfiguration.longPressTimeoutMillis

                                        val immediateDrag = withTimeoutOrNull(longPressTimeout) {
                                            var overSlop = Offset.Zero
                                            val dragChange = awaitTouchSlopOrCancellation(down.id) { change, over ->
                                                change.consume()
                                                overSlop = over
                                            }
                                            if (dragChange != null) Pair(dragChange, overSlop) else null
                                        }

                                        if (immediateDrag != null) {
                                            val (dragChange, overSlop) = immediateDrag
                                            startDragAt(dragChange.position)
                                            if (overSlop.y != 0f) {
                                                handleDragStep(dragChange.position, overSlop.y)
                                            }
                                            val finished = drag(dragChange.id) { change ->
                                                val delta = change.positionChange()
                                                change.consume()
                                                if (delta.y != 0f) {
                                                    handleDragStep(change.position, delta.y)
                                                }
                                            }
                                            finishDrag(commit = finished)
                                        } else if (currentEvent.changes.any { it.id == down.id && it.pressed }) {
                                            startDragAt(down.position)
                                            val finished = drag(down.id) { change ->
                                                val delta = change.positionChange()
                                                change.consume()
                                                if (delta.y != 0f) {
                                                    handleDragStep(change.position, delta.y)
                                                }
                                            }
                                            finishDrag(commit = finished)
                                        }
                                    }
                                }
                        )
                    }

                    item(key = "bottom_spacer") {
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
    itemModifier: Modifier = Modifier,
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
        modifier = itemModifier
            .fillMaxWidth()
            .zIndex(if (isBeingDragged) 10f else 1f)
            .graphicsLayer {
                translationY = dragOffsetY
                if (isBeingDragged) {
                    scaleX = 1.02f
                    scaleY = 1.02f
                }
            }
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
                    // Drag handle (supports both immediate drag and long-press drag)
                    Box(
                        modifier = dragModifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isBeingDragged) accentColor.copy(alpha = 0.16f) else Color.Transparent
                            ),
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

            // 6-Unit Countdown all on the same line: Years, Months, Days, Hours, Mins, and Secs
            SingleLineCountdownRow(
                breakdown = breakdown,
                accentColor = accentColor
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
