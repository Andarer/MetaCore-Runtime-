package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MyDarkColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    secondary = SecondaryTeal,
    tertiary = AccentPink,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceElevated,
    onPrimary = Color.White,
    onSecondary = Color(0xFF061516),
    onTertiary = Color.White,
    onBackground = Color(0xFFEDEDF5),
    onSurface = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFFD1D5DB)
)

private val MyLightColorScheme = lightColorScheme(
    primary = PrimaryNeon,
    secondary = SecondaryTeal,
    tertiary = AccentPink,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceElevated,
    onPrimary = Color.White,
    onSecondary = Color(0xFF061516),
    onTertiary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF4B5563)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to force our exquisite custom color identities
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MyDarkColorScheme
        else -> MyLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
