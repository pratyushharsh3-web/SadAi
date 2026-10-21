package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speech.AssistantState
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonSky

@Composable
fun VisualizerOrb(
    state: AssistantState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbAnimations")

    // Breathing pulse for idle
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Active wave pulse for listening
    val listenWave by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ListenWave"
    )

    // Rotation for thinking
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    // Speaking amplitude bounce
    val speakBounce by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SpeakBounce"
    )

    val currentScale = when (state) {
        AssistantState.LISTENING -> listenWave
        AssistantState.SPEAKING -> speakBounce
        AssistantState.THINKING -> 1.0f
        else -> pulseScale
    }

    val statusText = when (state) {
        AssistantState.LISTENING -> "Aapki aawaz sun rahi hoon ji... (Listening)"
        AssistantState.THINKING -> "Hmm... Soch rahi hoon... (Thinking)"
        AssistantState.SPEAKING -> "Jarvis bol rahi hai... (Tap to stop)"
        AssistantState.ERROR -> "Koi problem hui. Tap karke dobara bolein ji."
        AssistantState.IDLE -> "Tap to Speak • 'Hey Jarvis'"
    }

    val statusColor = when (state) {
        AssistantState.LISTENING -> ElectricCyan
        AssistantState.THINKING -> NeonSky
        AssistantState.SPEAKING -> Color(0xFF10B981)
        AssistantState.ERROR -> Color(0xFFEF4444)
        AssistantState.IDLE -> Color(0xFF94A3B8)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .testTag("voice_orb_button")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Outer dynamic animated Canvas aura
            Canvas(
                modifier = Modifier
                    .size(180.dp)
                    .scale(currentScale)
                    .rotate(if (state == AssistantState.THINKING) rotation else 0f)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val baseRadius = size.minDimension / 2.4f

                // Outer aura ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            when (state) {
                                AssistantState.LISTENING -> ElectricCyan.copy(alpha = 0.45f)
                                AssistantState.THINKING -> NeonSky.copy(alpha = 0.4f)
                                AssistantState.SPEAKING -> Color(0xFF10B981).copy(alpha = 0.4f)
                                else -> ElectricCyan.copy(alpha = 0.2f)
                            },
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.width / 2
                    )
                )

                // Concentric Tech Rings
                drawCircle(
                    color = ElectricCyan.copy(alpha = 0.4f),
                    radius = baseRadius,
                    style = Stroke(width = 2.dp.toPx())
                )

                if (state == AssistantState.LISTENING || state == AssistantState.THINKING) {
                    drawCircle(
                        color = NeonSky.copy(alpha = 0.6f),
                        radius = baseRadius * 1.18f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            // Central Glowing Sphere
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF),
                                Color(0xFF0284C7),
                                Color(0xFF0A192F)
                            )
                        )
                    )
                    .border(2.dp, ElectricCyan.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (state) {
                    AssistantState.LISTENING -> Icons.Default.Mic
                    AssistantState.THINKING -> Icons.Default.SmartToy
                    AssistantState.SPEAKING -> Icons.Default.VolumeUp
                    AssistantState.ERROR -> Icons.Default.MicOff
                    AssistantState.IDLE -> Icons.Default.Mic
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "Microphone Action",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State indicator pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurfaceVariant.copy(alpha = 0.85f))
                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
