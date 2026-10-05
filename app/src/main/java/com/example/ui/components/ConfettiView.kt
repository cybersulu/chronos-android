package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val targetX: Float,
    val targetY: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiCelebrationOverlay(
    modifier: Modifier = Modifier,
    particleCount: Int = 45
) {
    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFFF59E0B),
            Color(0xFF06B6D4),
            Color(0xFF8B5CF6),
            Color(0xFF10B981),
            Color(0xFFF43F5E),
            Color(0xFF3B82F6),
            Color(0xFFFBBF24)
        )
        val rnd = Random(System.currentTimeMillis())
        List(particleCount) {
            val startX = rnd.nextFloat()
            val startY = -0.1f - rnd.nextFloat() * 0.3f
            val endX = startX + (rnd.nextFloat() - 0.5f) * 0.6f
            val endY = 1.1f + rnd.nextFloat() * 0.2f
            ConfettiParticle(
                initialX = startX,
                initialY = startY,
                targetX = endX,
                targetY = endY,
                color = colors[rnd.nextInt(colors.size)],
                width = 12f + rnd.nextFloat() * 14f,
                height = 8f + rnd.nextFloat() * 12f,
                rotationSpeed = 360f * (rnd.nextFloat() * 4 - 2)
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3200, easing = LinearEasing)
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val currentProgress = progress.value

            particles.forEach { p ->
                val curX = (p.initialX + (p.targetX - p.initialX) * currentProgress) * w
                val curY = (p.initialY + (p.targetY - p.initialY) * currentProgress) * h
                val rotation = p.rotationSpeed * currentProgress

                rotate(degrees = rotation, pivot = Offset(curX, curY)) {
                    drawRect(
                        color = p.color.copy(alpha = (1f - currentProgress * 0.3f).coerceIn(0f, 1f)),
                        topLeft = Offset(curX - p.width / 2, curY - p.height / 2),
                        size = Size(p.width, p.height)
                    )
                }
            }
        }
    }
}
