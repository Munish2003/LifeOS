package com.personal.lifeos.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SkyBlueWhiteColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = PureWhite,
    primaryContainer = SkyBlueSurface,
    onPrimaryContainer = SkyBluePrimary,
    secondary = CyanAccent,
    onSecondary = PureWhite,
    tertiary = EmeraldSuccess,
    background = SkyBackground,
    onBackground = TextDarkPrimary,
    surface = PureWhite,
    onSurface = TextDarkPrimary,
    surfaceVariant = SkyBlueSurface,
    onSurfaceVariant = TextDarkSecondary,
    outline = CardBorderLight
)

@Composable
fun LifeOSTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = SkyBlueWhiteColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SkyBackground.toArgb()
            window.navigationBarColor = PureWhite.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
