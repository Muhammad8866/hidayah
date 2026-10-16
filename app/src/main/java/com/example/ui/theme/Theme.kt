package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Every container role is set explicitly: Material's defaults are purple, which leaked
// into the navigation-bar indicator, chips, switches and tabs.
private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = DarkBackground,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = TextPrimaryDark,
    inversePrimary = EmeraldMedium,
    secondary = MintAccent,
    onSecondary = DarkBackground,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = GoldMuted,
    onTertiary = DarkBackground,
    tertiaryContainer = GoldContainerDark,
    onTertiaryContainer = GoldContainerLight,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    surfaceTint = GoldAccent,
    surfaceDim = DarkBackground,
    surfaceBright = DarkSurfaceVariant,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceHighest,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = CreamSurface,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = EmeraldDark,
    inversePrimary = GoldAccent,
    secondary = EmeraldLight,
    onSecondary = CreamSurface,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = EmeraldDark,
    tertiary = GoldTertiaryLight,
    onTertiary = CreamSurface,
    tertiaryContainer = GoldContainerLight,
    onTertiaryContainer = GoldContainerDark,
    background = CreamBackground,
    onBackground = TextPrimaryLight,
    surface = CreamSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    surfaceTint = EmeraldPrimary,
    surfaceDim = CreamSurfaceVariant,
    surfaceBright = CreamSurface,
    surfaceContainerLowest = CreamSurface,
    surfaceContainerLow = CreamSurface,
    surfaceContainer = CreamSurface,
    surfaceContainerHigh = CreamSurfaceVariant,
    surfaceContainerHighest = CreamSurfaceHighest,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand aesthetic consistent by default
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

    MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = AppShapes, content = content)
}
