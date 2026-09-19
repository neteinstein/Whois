package com.neteinstein.whois.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.neteinstein.whois.core.ui.strings.LocalStrings
import com.neteinstein.whois.core.ui.theme.Motion
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val isReady by viewModel.isReady.collectAsState()

    LaunchedEffect(isReady) {
        if (isReady) onFinished()
    }

    val strings = LocalStrings.current
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, animationSpec = tween(Motion.DURATION_LONG, easing = EaseOutBack))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash-loop")
    val glassSway by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = Motion.emphasized),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glass-sway",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(entrance.value)
                    .rotate(glassSway),
                contentAlignment = Alignment.Center,
            ) {
                MagnifyingGlassOverSilhouette()
            }
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = strings.splashTagline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A simple person silhouette with a magnifying glass hovering over it, drawn with primitives. */
@Composable
private fun MagnifyingGlassOverSilhouette(modifier: Modifier = Modifier) {
    val silhouetteColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val glassColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.size(180.dp)) {
        val w = size.width
        val h = size.height

        // Person silhouette: head + shoulders, anchored toward the bottom.
        val headRadius = w * 0.14f
        val headCenter = Offset(w * 0.5f, h * 0.42f)
        drawCircle(color = silhouetteColor, radius = headRadius, center = headCenter)

        val shoulderTop = headCenter.y + headRadius * 0.9f
        drawArc(
            color = silhouetteColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.12f, shoulderTop),
            size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.55f),
        )

        // Magnifying glass: ring + handle, offset toward the top-right of the head.
        val glassCenter = Offset(w * 0.62f, h * 0.34f)
        val glassRadius = w * 0.24f
        drawCircle(
            color = glassColor,
            radius = glassRadius,
            center = glassCenter,
            style = Stroke(width = w * 0.045f, cap = StrokeCap.Round),
        )
        val handleStart = Offset(
            glassCenter.x + glassRadius * 0.75f,
            glassCenter.y + glassRadius * 0.75f,
        )
        val handleEnd = Offset(handleStart.x + w * 0.16f, handleStart.y + w * 0.16f)
        drawLine(
            color = glassColor,
            start = handleStart,
            end = handleEnd,
            strokeWidth = w * 0.05f,
            cap = StrokeCap.Round,
        )
    }
}
