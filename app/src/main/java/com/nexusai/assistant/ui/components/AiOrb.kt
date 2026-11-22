package com.nexusai.assistant.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexusai.assistant.ui.theme.NexusBlue
import com.nexusai.assistant.ui.theme.NexusCyan

enum class OrbState { Idle, Listening, Thinking, Speaking }

@Composable
fun AiOrb(
    state: OrbState,
    modifier: Modifier = Modifier,
    orbSize: Dp = 160.dp
) {
    val transition = rememberInfiniteTransition(label = "orb")
    val pulseMs = when (state) {
        OrbState.Idle -> 3000
        OrbState.Thinking -> 1200
        OrbState.Listening -> 1600
        OrbState.Speaking -> 700
    }
    val pulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier.size(orbSize)) {
        val c = center
        val r = this.size.minDimension / 2f

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NexusCyan.copy(alpha = 0.35f), Color.Transparent),
                center = c,
                radius = r * pulse
            ),
            radius = r * pulse,
            center = c
        )

        // Core
        val coreRadius = r * 0.5f * pulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.9f), NexusCyan, NexusBlue),
                center = c,
                radius = coreRadius
            ),
            radius = coreRadius,
            center = c
        )

        // Rotating ring
        val ringRadius = r * 0.72f
        rotate(degrees = rotation, pivot = c) {
            drawArc(
                color = NexusCyan.copy(alpha = 0.8f),
                startAngle = 0f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(c.x - ringRadius, c.y - ringRadius),
                size = Size(ringRadius * 2, ringRadius * 2),
                style = Stroke(width = 4.dp.toPx())
            )
            drawArc(
                color = NexusBlue.copy(alpha = 0.6f),
                startAngle = 180f,
                sweepAngle = 60f,
                useCenter = false,
                topLeft = Offset(c.x - ringRadius, c.y - ringRadius),
                size = Size(ringRadius * 2, ringRadius * 2),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
