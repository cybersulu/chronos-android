package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.model.CountdownCategories

data class CategoryCountItem(
    val name: String,
    val count: Int
)

@Composable
fun CategoryFilterBar(
    categoryCounts: List<CategoryCountItem>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onAddCategoryClick: (() -> Unit)? = null,
    onManageCategoriesClick: (() -> Unit)? = null,
    onEditSelectedCategory: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("category_filter_bar"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(categoryCounts, key = { it.name }) { item ->
            val isSelected = item.name.equals(selectedCategory, ignoreCase = true)
            val categoryColor = if (item.name == CountdownCategories.ALL) {
                MaterialTheme.colorScheme.primary
            } else {
                Color(CountdownCategories.getColorHexForCategory(item.name))
            }

            val icon = if (item.name == CountdownCategories.ALL) {
                Icons.Default.AllInclusive
            } else {
                CountdownCategories.getIconForCategory(item.name)
            }

            val pillShape = RoundedCornerShape(16.dp)

            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(pillShape)
                    .background(
                        if (isSelected) categoryColor.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) categoryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = pillShape
                    )
                    .clickable { onCategorySelected(item.name) }
                    .padding(horizontal = 14.dp)
                    .testTag("category_chip_${item.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurface
                )

                // Distinct Category Counter Badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isSelected) categoryColor
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.count.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // If selected and editable, show quick edit pencil
                if (isSelected && item.name != CountdownCategories.ALL && onEditSelectedCategory != null) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onEditSelectedCategory(item.name) }
                            .padding(4.dp)
                            .testTag("button_edit_selected_category_${item.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Category",
                            tint = categoryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (onManageCategoriesClick != null) {
            item(key = "manage_categories_button") {
                val pillShape = RoundedCornerShape(16.dp)
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(pillShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = pillShape
                        )
                        .clickable { onManageCategoriesClick() }
                        .padding(horizontal = 14.dp)
                        .testTag("category_chip_manage"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Manage",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Manage",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (onAddCategoryClick != null) {
            item(key = "add_category_button") {
                val pillShape = RoundedCornerShape(16.dp)
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(pillShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = pillShape
                        )
                        .clickable { onAddCategoryClick() }
                        .padding(horizontal = 14.dp)
                        .testTag("category_chip_add_new"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Category",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "New Category",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
