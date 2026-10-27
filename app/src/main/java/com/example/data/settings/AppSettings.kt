package com.example.data.settings

import androidx.annotation.StringRes
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.R

/**
 * Languages the interface is translated into. To add one, create `res/values-<tag>/strings.xml`
 * and add an entry here. The clinical questions and advice inside `triage_tree.json` are
 * English only; this setting changes the app interface.
 */
enum class AppLanguage(val tag: String, val nativeName: String, val englishName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "हिन्दी", "Hindi"),
    BENGALI("bn", "বাংলা", "Bengali"),
    MARATHI("mr", "मराठी", "Marathi"),
    TAMIL("ta", "தமிழ்", "Tamil"),
    TELUGU("te", "తెలుగు", "Telugu");

    companion object {
        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: ENGLISH
    }
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class TextSize(val scale: Float) {
    NORMAL(1f),
    LARGE(1.15f),
    EXTRA_LARGE(1.3f)
}

data class AppSettings(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textSize: TextSize = TextSize.NORMAL,
    /** Number dialled by every emergency button in the app. One of [EmergencyNumbers.PRIMARY_OPTIONS]. */
    val emergencyNumber: String = EmergencyNumbers.NATIONAL,
    val showHomeRemedies: Boolean = true,
    val saveHistory: Boolean = true
)

/** Indian emergency numbers. 112 works nationwide; 108 and 102 ambulance cover varies by state. */
object EmergencyNumbers {
    const val NATIONAL = "112"
    const val AMBULANCE_108 = "108"
    const val AMBULANCE_102 = "102"

    /** Numbers the user may choose as the one-tap emergency number. */
    val PRIMARY_OPTIONS = listOf(NATIONAL, AMBULANCE_108, AMBULANCE_102)

    data class Helpline(val number: String, @StringRes val label: Int, @StringRes val note: Int)

    val HELPLINES = listOf(
        Helpline("112", R.string.helpline_112, R.string.helpline_112_note),
        Helpline("108", R.string.helpline_108, R.string.helpline_108_note),
        Helpline("102", R.string.helpline_102, R.string.helpline_102_note),
        Helpline("100", R.string.helpline_100, R.string.helpline_100_note),
        Helpline("101", R.string.helpline_101, R.string.helpline_101_note),
        Helpline("1098", R.string.helpline_1098, R.string.helpline_1098_note),
        Helpline("14416", R.string.helpline_14416, R.string.helpline_14416_note)
    )
}

/** Settings visible to every screen, so emergency buttons dial the chosen number. */
val LocalAppSettings = staticCompositionLocalOf { AppSettings() }
