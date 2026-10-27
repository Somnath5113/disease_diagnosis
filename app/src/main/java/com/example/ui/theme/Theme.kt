package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.example.data.model.UrgencyLevel

/**
 * Modern Material 3 Light Color Scheme for MedTriage.
 * Crisp, clean, authoritative clinical appearance.
 */
val ModernCrispLightColorScheme = lightColorScheme(
    primary = MedicalTealLight,
    onPrimary = MedicalTealOnLight,
    primaryContainer = MedicalTealContainerLight,
    onPrimaryContainer = MedicalTealOnContainerLight,
    secondary = SlateNavyLight,
    onSecondary = SlateNavyOnLight,
    secondaryContainer = SlateNavyContainerLight,
    onSecondaryContainer = SlateNavyOnContainerLight,
    tertiary = RestorativeEmeraldLight,
    onTertiary = RestorativeEmeraldOnLight,
    tertiaryContainer = RestorativeEmeraldContainerLight,
    onTertiaryContainer = RestorativeEmeraldOnContainerLight,
    background = BackgroundLight,
    onBackground = TextHeadlinesLight,
    surface = SurfaceLight,
    onSurface = TextHeadlinesLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextMutedLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = EmergencyRedLight,
    errorContainer = EmergencyContainerLight,
    onError = MedicalTealOnLight,
    onErrorContainer = OnEmergencyContainerLight
)

/**
 * Modern Material 3 Dark Color Scheme for MedTriage.
 * Deep slate ocean tones designed for low-light clinical and nighttime symptom intake.
 */
val ModernCrispDarkColorScheme = darkColorScheme(
    primary = MedicalTealDark,
    onPrimary = MedicalTealOnDark,
    primaryContainer = MedicalTealContainerDark,
    onPrimaryContainer = MedicalTealOnContainerDark,
    secondary = SlateNavyDark,
    onSecondary = SlateNavyOnDark,
    secondaryContainer = SlateNavyContainerDark,
    onSecondaryContainer = SlateNavyOnContainerDark,
    tertiary = RestorativeEmeraldDark,
    onTertiary = RestorativeEmeraldOnDark,
    tertiaryContainer = RestorativeEmeraldContainerDark,
    onTertiaryContainer = RestorativeEmeraldOnContainerDark,
    background = BackgroundDark,
    onBackground = TextHeadlinesDark,
    surface = SurfaceDark,
    onSurface = TextHeadlinesDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMutedDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = EmergencyRedDark,
    errorContainer = EmergencyContainerDark,
    onError = EmergencyContainerDark,
    onErrorContainer = OnEmergencyContainerDark
)

/**
 * Provides theme-aware urgency tokens based on the current theme (Light vs Dark).
 */
@Composable
@ReadOnlyComposable
fun getUrgencyThemeColors(level: UrgencyLevel): UrgencyColors {
    val isDark = isSystemInDarkTheme()
    return when (level) {
        UrgencyLevel.EMERGENCY -> if (isDark) {
            UrgencyColors(
                primary = EmergencyRedDark,
                container = EmergencyContainerDark,
                onContainer = OnEmergencyContainerDark,
                border = EmergencyBorderDark
            )
        } else {
            UrgencyColors(
                primary = EmergencyRedLight,
                container = EmergencyContainerLight,
                onContainer = OnEmergencyContainerLight,
                border = EmergencyBorderLight
            )
        }
        UrgencyLevel.URGENT_CARE -> if (isDark) {
            UrgencyColors(
                primary = UrgentOrangeDark,
                container = UrgentContainerDark,
                onContainer = OnUrgentContainerDark,
                border = UrgentBorderDark
            )
        } else {
            UrgencyColors(
                primary = UrgentOrangeLight,
                container = UrgentContainerLight,
                onContainer = OnUrgentContainerLight,
                border = UrgentBorderLight
            )
        }
        UrgencyLevel.ROUTINE_DOCTOR -> if (isDark) {
            UrgencyColors(
                primary = RoutineBlueDark,
                container = RoutineContainerDark,
                onContainer = OnRoutineContainerDark,
                border = RoutineBorderDark
            )
        } else {
            UrgencyColors(
                primary = RoutineBlueLight,
                container = RoutineContainerLight,
                onContainer = OnRoutineContainerLight,
                border = RoutineBorderLight
            )
        }
        UrgencyLevel.SELF_CARE -> if (isDark) {
            UrgencyColors(
                primary = SelfCareGreenDark,
                container = SelfCareContainerDark,
                onContainer = OnSelfCareContainerDark,
                border = SelfCareBorderDark
            )
        } else {
            UrgencyColors(
                primary = SelfCareGreenLight,
                container = SelfCareContainerLight,
                onContainer = OnSelfCareContainerLight,
                border = SelfCareBorderLight
            )
        }
    }
}

@Composable
@ReadOnlyComposable
fun getUrgencyThemeColors(levelString: String): UrgencyColors {
    val level = try {
        UrgencyLevel.valueOf(levelString)
    } catch (_: Exception) {
        UrgencyLevel.ROUTINE_DOCTOR
    }
    return getUrgencyThemeColors(level)
}

@Composable
fun MedTriageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = if (darkTheme) {
        ModernCrispDarkColorScheme
    } else {
        ModernCrispLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
