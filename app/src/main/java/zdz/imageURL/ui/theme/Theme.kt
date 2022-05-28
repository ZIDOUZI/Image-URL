package zdz.imageURL.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006a5f),
    onPrimary = Color(0xFFffffff),
    primaryContainer = Color(0xFF56fae6),
    onPrimaryContainer = Color(0xFF00201c),
    secondary = Color(0xFF98405f),
    onSecondary = Color(0xFFffffff),
    secondaryContainer = Color(0xFFffd9e3),
    onSecondaryContainer = Color(0xFF3e001b),
    tertiary = Color(0xFF006685),
    onTertiary = Color(0xFFffffff),
    tertiaryContainer = Color(0xFFbce9ff),
    onTertiaryContainer = Color(0xFF001f2a),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFfdfbff),
    onBackground = Color(0xFF1a1b1f),
    surface = Color(0xFFfdfbff),
    onSurface = Color(0xFF1a1b1f),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    inverseOnSurface = Color(0xFFf2f0f4),
    inverseSurface = Color(0xFF2f3033),
    inversePrimary = Color(0xFF28ddca),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF28ddca),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005047),
    onPrimaryContainer = Color(0xFF56fae6),
    secondary = Color(0xFFffb1c9),
    onSecondary = Color(0xFF5e1031),
    secondaryContainer = Color(0xFF7b2948),
    onSecondaryContainer = Color(0xFFffd9e3),
    tertiary = Color(0xFF65d3ff),
    onTertiary = Color(0xFF003547),
    tertiaryContainer = Color(0xFF004d66),
    onTertiaryContainer = Color(0xFFbce9ff),
    error = Color(0xFFF2B8B5),
    errorContainer = Color(0xFF8C1D18),
    onError = Color(0xFF601410),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1a1b1f),
    onBackground = Color(0xFFe3e2e6),
    surface = Color(0xFF1a1b1f),
    onSurface = Color(0xFFe3e2e6),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    inverseOnSurface = Color(0xFF1a1b1f),
    inverseSurface = Color(0xFFe3e2e6),
    inversePrimary = Color(0xFF006a5f),
)

private fun getTransparentLightColorScheme(alpha: Float = 0f) =
    LightColorScheme.copy(background = LightColorScheme.background.copy(alpha = alpha))

private fun getTransparentDarkColorScheme(alpha: Float = 0f) =
    DarkColorScheme.copy(background = DarkColorScheme.background.copy(alpha = alpha))

@Composable
fun ImageURLTheme(
    transparent: Boolean = false,
    darkTheme: Boolean? = null,
    alpha: Float = 0f,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val dark = darkTheme ?: isSystemInDarkTheme()
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark && transparent -> getTransparentDarkColorScheme(alpha)
        dark -> DarkColorScheme
        transparent -> getTransparentLightColorScheme(alpha)
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = dark
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}