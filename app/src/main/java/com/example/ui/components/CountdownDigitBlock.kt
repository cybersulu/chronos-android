package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CountdownBreakdown

@Composable
fun CountdownDigitBlock(
    value: Long,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val shape = RoundedCornerShape(if (isLarge) 16.dp else 12.dp)
    val formattedValue = if (value < 10 && value >= 0) "0$value" else value.toString()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = if (isLarge) 68.dp else 44.dp)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.18f),
                            accentColor.copy(alpha = 0.06f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.45f),
                            accentColor.copy(alpha = 0.15f)
                        )
                    ),
                    shape = shape
                )
                .padding(
                    horizontal = if (isLarge) 12.dp else 6.dp,
                    vertical = if (isLarge) 14.dp else 8.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = formattedValue,
                transitionSpec = {
                    slideInVertically { height -> height } togetherWith
                            slideOutVertically { height -> -height }
                },
                label = "digit_anim"
            ) { targetText ->
                Text(
                    text = targetText,
                    style = if (isLarge) {
                        MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    } else {
                        MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    color = accentColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (isLarge) 11.sp else 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FullCountdownGrid(
    breakdown: CountdownBreakdown,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (isLarge) 12.dp else 8.dp)
    ) {
        // Row 1: Years, Months, Days
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CountdownDigitBlock(
                value = breakdown.years,
                label = "Years",
                accentColor = accentColor,
                isLarge = isLarge,
                modifier = Modifier.weight(1f)
            )
            CountdownDigitBlock(
                value = breakdown.months,
                label = "Months",
                accentColor = accentColor,
                isLarge = isLarge,
                modifier = Modifier.weight(1f)
            )
            CountdownDigitBlock(
                value = breakdown.days,
                label = "Days",
                accentColor = accentColor,
                isLarge = isLarge,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Hours, Minutes, Seconds
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CountdownDigitBlock(
                value = breakdown.hours,
                label = "Hours",
                accentColor = accentColor,
                isLarge = isLarge,
                modifier = Modifier.weight(1f)
            )
            CountdownDigitBlock(
                value = breakdown.minutes,
                label = "Mins",
                accentColor = accentColor,
                isLarge = isLarge,
                modifier = Modifier.weight(1f)
            )
            CountdownDigitBlock(
                value = breakdown.seconds,
                label = "Secs",
                accentColor = accentColor,
                isLarge = isLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
