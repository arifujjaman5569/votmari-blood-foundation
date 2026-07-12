package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkRosePrimary,
    secondary = DarkRoseSecondary,
    tertiary = DarkRoseTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = OnRose,
    onBackground = OnDarkBackground,
    onSurface = OnDarkSurface
)

private val LightColorScheme = lightColorScheme(
    primary = RosePrimary,
    secondary = RoseSecondary,
    tertiary = RoseTertiary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = OnRose,
    onBackground = OnLightBackground,
    onSurface = OnLightSurface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false by default to enforce our premium Rose & Slate theme identity!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Custom modifier extension to apply a high-fidelity Frosted Glass effect in Jetpack Compose.
 * Simulates real glassmorphism by blending light shadow, subtle transparent overlay, and a crisp white reflection border.
 */
fun Modifier.frostedGlass(
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    lightColor: Color = Color.White.copy(alpha = 0.65f),
    darkColor: Color = Color.White.copy(alpha = 0.12f),
    isDark: Boolean = false,
    shadowElevation: Dp = 4.dp
): Modifier {
    val baseColor = if (isDark) darkColor else lightColor
    val borderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.75f)
    return this
        .shadow(elevation = shadowElevation, shape = RoundedCornerShape(cornerRadius), clip = false)
        .clip(RoundedCornerShape(cornerRadius))
        .background(baseColor)
        .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(cornerRadius))
}

