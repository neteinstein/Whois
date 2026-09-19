package com.neteinstein.whois.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

/** Shared motion tokens so every screen transition/animation feels like one system. */
object Motion {
    val emphasized = CubicBezierEasing(0.2f, 0.0f, 0f, 1.0f)
    const val DURATION_SHORT = 200
    const val DURATION_MEDIUM = 400
    const val DURATION_LONG = 600

    fun <T> emphasizedTween(durationMillis: Int = DURATION_MEDIUM) =
        tween<T>(durationMillis = durationMillis, easing = emphasized)
}
