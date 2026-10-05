package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryEntity
import com.example.model.CountdownCategories

@Composable
fun CategoryEditDialog(
    categoryToEdit: CategoryEntity? = null,
    onDismissRequest: () -> Unit,
    onSaveCategory: (name: String, iconName: String, colorHex: Long) -> Unit,
    onDeleteCategory: (() -> Unit)? = null,
    existingCategoryNames: List<String> = emptyList()
) {
    val isEditing = categoryToEdit != null
    var categoryName by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedIconName by remember { mutableStateOf(categoryToEdit?.iconName ?: "Event") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Each category uses the default accent color set for the category
    val defaultColorHex = CountdownCategories.getColorHexForCategory(
        categoryName.ifBlank { categoryToEdit?.name ?: "Celebration" }
    )
    val currentColor = Color(defaultColorHex)
    val selectedIcon = CountdownCategories.getIconByName(selectedIconName)

    if (showDeleteConfirm && onDeleteCategory != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Category?") },
            text = { Text("Are you sure you want to delete '${categoryToEdit?.name}'? Events in this category will keep their name.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteCategory()
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag("dialog_category_editor"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Category,
                        contentDescription = null,
                        tint = currentColor
                    )
                    Text(
                        text = if (isEditing) "Edit Category" else "New Category",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (isEditing && onDeleteCategory != null) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("button_delete_category")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Category",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Input
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        errorMessage = null
                    },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Fitness, Travel, Celebration") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentColor,
                        focusedLabelColor = currentColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_category_name")
                )

                // Live Preview Pill with default accent color for the category
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Category Preview",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(currentColor.copy(alpha = 0.22f))
                            .border(1.5.dp, currentColor, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = selectedIcon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = currentColor
                            )
                            Text(
                                text = if (categoryName.isBlank()) "Category Name" else categoryName.trim(),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = currentColor
                            )
                        }
                    }
                }

                // Icon Selection Grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Select Icon",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(CountdownCategories.AVAILABLE_ICONS, key = { it.first }) { (iconKey, iconVector) ->
                            val isIconSelected = selectedIconName == iconKey
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isIconSelected) currentColor.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = if (isIconSelected) 1.5.dp else 0.5.dp,
                                        color = if (isIconSelected) currentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedIconName = iconKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = iconKey,
                                    tint = if (isIconSelected) currentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = categoryName.trim()
                    if (trimmed.isBlank()) {
                        errorMessage = "Please enter a category name"
                    } else if (
                        !trimmed.equals(categoryToEdit?.name, ignoreCase = true) &&
                        existingCategoryNames.any { it.equals(trimmed, ignoreCase = true) }
                    ) {
                        errorMessage = "Category already exists"
                    } else {
                        onSaveCategory(trimmed, selectedIconName, defaultColorHex)
                        onDismissRequest()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("button_confirm_category_save")
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isEditing) "Save Changes" else "Save Category", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("button_cancel_category_editor")
            ) {
                Text("Cancel")
            }
        }
    )
}

// Backwards-compatible alias for AddCategoryDialog
@Composable
fun AddCategoryDialog(
    onDismissRequest: () -> Unit,
    onCategoryCreated: (name: String, iconName: String, colorHex: Long) -> Unit,
    existingCategoryNames: List<String> = emptyList()
) {
    CategoryEditDialog(
        categoryToEdit = null,
        onDismissRequest = onDismissRequest,
        onSaveCategory = onCategoryCreated,
        existingCategoryNames = existingCategoryNames
    )
}
