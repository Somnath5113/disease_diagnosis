package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Strict Modern Light-Only Color Scheme with Deep, Rich Colors.
 * Dark mode has been deliberately omitted per user design direction.
 */
private val ModernCrispLightColorScheme = lightColorScheme(
    primary = DeepTealPrimary,
    onPrimary = OnDeepTealPrimary,
    primaryContainer = SoftAquaContainer,
    onPrimaryContainer = OnSoftAquaContainer,
    secondary = DeepNavySecondary,
    onSecondary = OnDeepNavySecondary,
    secondaryContainer = SoftNavyContainer,
    onSecondaryContainer = OnSoftNavyContainer,
    tertiary = DeepJadeTertiary,
    onTertiary = OnDeepJadeTertiary,
    tertiaryContainer = SoftJadeContainer,
    onTertiaryContainer = OnSoftJadeContainer,
    background = CrispCanvasBackground,
    onBackground = DeepTextHeadlines,
    surface = PureWhiteSurface,
    onSurface = DeepTextHeadlines,
    surfaceVariant = SurfaceMutedFrost,
    onSurfaceVariant = MutedTextSlate,
    outline = CardBorderSubtle,
    outlineVariant = CardBorderFocused,
    error = DeepEmergencyRed,
    errorContainer = LightEmergencyContainer,
    onError = OnDeepTealPrimary,
    onErrorContainer = OnLightEmergencyContainer
)

@Composable
fun MedTriageTheme(
    darkTheme: Boolean = false, // Forced Light Mode Only
    dynamicColor: Boolean = false, // Preserves intentional deep-color identity
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ModernCrispLightColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MedTriageTheme(darkTheme = false, dynamicColor = false, content = content)
}
