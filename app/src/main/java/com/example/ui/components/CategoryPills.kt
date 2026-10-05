package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
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
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("category_filter_bar"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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

            val pillShape = RoundedCornerShape(20.dp)

            Row(
                modifier = Modifier
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
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("category_chip_${item.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
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
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.count.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (onAddCategoryClick != null) {
            item(key = "add_category_button") {
                val pillShape = RoundedCornerShape(20.dp)
                Row(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = pillShape
                        )
                        .clickable { onAddCategoryClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("category_chip_add_new"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Category",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "New Category",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
