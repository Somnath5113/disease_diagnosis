package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.UrgencyLevel

// ==========================================
// Modern Clinical Color Palette (Material 3)
// Light & Dark Modes with High Contrast & Clinical Depth
// ==========================================

// --- Primary: Precision Medical Teal ---
val MedicalTealLight = Color(0xFF006874)
val MedicalTealOnLight = Color(0xFFFFFFFF)
val MedicalTealContainerLight = Color(0xFFE2F4F7)
val MedicalTealOnContainerLight = Color(0xFF002024)

val MedicalTealDark = Color(0xFF4DD8E6)
val MedicalTealOnDark = Color(0xFF00363D)
val MedicalTealContainerDark = Color(0xFF004F58)
val MedicalTealOnContainerDark = Color(0xFF9EEFFD)

// --- Secondary: Deep Slate Navy ---
val SlateNavyLight = Color(0xFF1E293B)
val SlateNavyOnLight = Color(0xFFFFFFFF)
val SlateNavyContainerLight = Color(0xFFF1F5F9)
val SlateNavyOnContainerLight = Color(0xFF0F172A)

val SlateNavyDark = Color(0xFF94A3B8)
val SlateNavyOnDark = Color(0xFF0F172A)
val SlateNavyContainerDark = Color(0xFF1E293B)
val SlateNavyOnContainerDark = Color(0xFFF8FAFC)

// --- Tertiary: Restorative Emerald ---
val RestorativeEmeraldLight = Color(0xFF059669)
val RestorativeEmeraldOnLight = Color(0xFFFFFFFF)
val RestorativeEmeraldContainerLight = Color(0xFFECFDF5)
val RestorativeEmeraldOnContainerLight = Color(0xFF064E3B)

val RestorativeEmeraldDark = Color(0xFF34D399)
val RestorativeEmeraldOnDark = Color(0xFF064E3B)
val RestorativeEmeraldContainerDark = Color(0xFF065F46)
val RestorativeEmeraldOnContainerDark = Color(0xFFA7F3D0)

// --- Surfaces & Backgrounds ---
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val TextHeadlinesLight = Color(0xFF0F172A)
val TextBodyLight = Color(0xFF334155)
val TextMutedLight = Color(0xFF64748B)
val OutlineLight = Color(0xFFE2E8F0)
val OutlineVariantLight = Color(0xFFCBD5E1)

val BackgroundDark = Color(0xFF0B111A)
val SurfaceDark = Color(0xFF131D28)
val SurfaceVariantDark = Color(0xFF1C2736)
val TextHeadlinesDark = Color(0xFFF8FAFC)
val TextBodyDark = Color(0xFFCBD5E1)
val TextMutedDark = Color(0xFF94A3B8)
val OutlineDark = Color(0xFF283648)
val OutlineVariantDark = Color(0xFF3A4B62)

// ==========================================
// Semantic Urgency Tiers (Light & Dark Ready)
// ==========================================

data class UrgencyColors(
    val primary: Color,
    val container: Color,
    val onContainer: Color,
    val border: Color
)

// Critical Emergency: Vivid Crimson
val EmergencyRedLight = Color(0xFFDC2626)
val EmergencyContainerLight = Color(0xFFFEF2F2)
val OnEmergencyContainerLight = Color(0xFF991B1B)
val EmergencyBorderLight = Color(0xFFFECACA)

val EmergencyRedDark = Color(0xFFF87171)
val EmergencyContainerDark = Color(0xFF3B1214)
val OnEmergencyContainerDark = Color(0xFFFEE2E2)
val EmergencyBorderDark = Color(0xFF7F1D1D)

// Urgent Medical Attention: Warm Amber / Tangerine
val UrgentOrangeLight = Color(0xFFD97706)
val UrgentContainerLight = Color(0xFFFFFBEB)
val OnUrgentContainerLight = Color(0xFF92400E)
val UrgentBorderLight = Color(0xFFFDE68A)

val UrgentOrangeDark = Color(0xFFFBBF24)
val UrgentContainerDark = Color(0xFF3A1E05)
val OnUrgentContainerDark = Color(0xFFFEF3C7)
val UrgentBorderDark = Color(0xFF78350F)

// Routine Physician Evaluation: Cobalt Blue
val RoutineBlueLight = Color(0xFF2563EB)
val RoutineContainerLight = Color(0xFFEFF6FF)
val OnRoutineContainerLight = Color(0xFF1E40AF)
val RoutineBorderLight = Color(0xFFBFDBFE)

val RoutineBlueDark = Color(0xFF60A5FA)
val RoutineContainerDark = Color(0xFF142442)
val OnRoutineContainerDark = Color(0xFFDBEAFE)
val RoutineBorderDark = Color(0xFF1E3A8A)

// Supportive Self-Care: Forest Emerald
val SelfCareGreenLight = Color(0xFF059669)
val SelfCareContainerLight = Color(0xFFECFDF5)
val OnSelfCareContainerLight = Color(0xFF065F46)
val SelfCareBorderLight = Color(0xFFA7F3D0)

val SelfCareGreenDark = Color(0xFF34D399)
val SelfCareContainerDark = Color(0xFF083321)
val OnSelfCareContainerDark = Color(0xFFA7F3D0)
val SelfCareBorderDark = Color(0xFF047857)

// Backward-compatibility aliases for existing code references
val DeepTealPrimary = MedicalTealLight
val OnDeepTealPrimary = MedicalTealOnLight
val SoftAquaContainer = MedicalTealContainerLight
val OnSoftAquaContainer = MedicalTealOnContainerLight

val DeepNavySecondary = SlateNavyLight
val OnDeepNavySecondary = SlateNavyOnLight
val SoftNavyContainer = SlateNavyContainerLight
val OnSoftNavyContainer = SlateNavyOnContainerLight

val DeepJadeTertiary = RestorativeEmeraldLight
val OnDeepJadeTertiary = RestorativeEmeraldOnLight
val SoftJadeContainer = RestorativeEmeraldContainerLight
val OnSoftJadeContainer = RestorativeEmeraldOnContainerLight

val PureWhiteSurface = SurfaceLight
val CrispCanvasBackground = BackgroundLight
val DeepTextHeadlines = TextHeadlinesLight
val DeepTextBody = TextBodyLight
val MutedTextSlate = TextMutedLight

val CardBorderSubtle = OutlineLight
val CardBorderFocused = OutlineVariantLight
val SurfaceMutedFrost = SurfaceVariantLight

val DeepEmergencyRed = EmergencyRedLight
val LightEmergencyContainer = EmergencyContainerLight
val OnLightEmergencyContainer = OnEmergencyContainerLight
val BorderEmergencyRed = EmergencyBorderLight

val DeepUrgentOrange = UrgentOrangeLight
val LightUrgentContainer = UrgentContainerLight
val OnLightUrgentContainer = OnUrgentContainerLight
val BorderUrgentOrange = UrgentBorderLight

val DeepRoutineBlue = RoutineBlueLight
val LightRoutineContainer = RoutineContainerLight
val OnLightRoutineContainer = OnRoutineContainerLight
val BorderRoutineBlue = RoutineBorderLight

val DeepSelfCareGreen = SelfCareGreenLight
val LightSelfCareContainer = SelfCareContainerLight
val OnLightSelfCareContainer = OnSelfCareContainerLight
val BorderSelfCareGreen = SelfCareBorderLight

val UrgencyEmergencyRed = EmergencyRedLight
val UrgencyEmergencyRedContainer = EmergencyContainerLight
val UrgencyEmergencyRedOnContainer = OnEmergencyContainerLight
val UrgencyEmergencyRedBorder = EmergencyBorderLight

val UrgencyUrgentOrange = UrgentOrangeLight
val UrgencyUrgentOrangeContainer = UrgentContainerLight
val UrgencyUrgentOrangeOnContainer = OnUrgentContainerLight
val UrgencyUrgentOrangeBorder = UrgentBorderLight

val UrgencyRoutineBlue = RoutineBlueLight
val UrgencyRoutineBlueContainer = RoutineContainerLight
val UrgencyRoutineBlueOnContainer = OnRoutineContainerLight
val UrgencyRoutineBlueBorder = RoutineBorderLight

val UrgencySelfCareGreen = SelfCareGreenLight
val UrgencySelfCareGreenContainer = SelfCareContainerLight
val UrgencySelfCareGreenOnContainer = OnSelfCareContainerLight
val UrgencySelfCareGreenBorder = SelfCareBorderLight
