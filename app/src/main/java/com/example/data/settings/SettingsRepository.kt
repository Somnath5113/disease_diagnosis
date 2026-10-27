package com.example.data.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Persists [AppSettings] in SharedPreferences. SharedPreferences is read synchronously, which
 * the language setting needs: the locale must be known in `attachBaseContext`, before any UI exists.
 */
class SettingsRepository private constructor(private val appContext: Context) {
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun load(): AppSettings {
        val number = prefs.getString(KEY_EMERGENCY_NUMBER, null)
        return AppSettings(
            language = AppLanguage.fromTag(prefs.getString(KEY_LANGUAGE, null)),
            themeMode = enumOrDefault(prefs.getString(KEY_THEME, null), ThemeMode.SYSTEM),
            textSize = enumOrDefault(prefs.getString(KEY_TEXT_SIZE, null), TextSize.NORMAL),
            emergencyNumber = if (number in EmergencyNumbers.PRIMARY_OPTIONS) number!! else EmergencyNumbers.NATIONAL,
            showHomeRemedies = prefs.getBoolean(KEY_REMEDIES, true),
            saveHistory = prefs.getBoolean(KEY_SAVE_HISTORY, true)
        )
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.tag).apply()
        _settings.value = _settings.value.copy(language = language)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun setTextSize(size: TextSize) {
        prefs.edit().putString(KEY_TEXT_SIZE, size.name).apply()
        _settings.value = _settings.value.copy(textSize = size)
    }

    fun setEmergencyNumber(number: String) {
        if (number !in EmergencyNumbers.PRIMARY_OPTIONS) return
        prefs.edit().putString(KEY_EMERGENCY_NUMBER, number).apply()
        _settings.value = _settings.value.copy(emergencyNumber = number)
    }

    fun setShowHomeRemedies(show: Boolean) {
        prefs.edit().putBoolean(KEY_REMEDIES, show).apply()
        _settings.value = _settings.value.copy(showHomeRemedies = show)
    }

    fun setSaveHistory(save: Boolean) {
        prefs.edit().putBoolean(KEY_SAVE_HISTORY, save).apply()
        _settings.value = _settings.value.copy(saveHistory = save)
    }

    private inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: default

    companion object {
        private const val PREFS_NAME = "medtriage_settings"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_TEXT_SIZE = "text_size"
        private const val KEY_EMERGENCY_NUMBER = "emergency_number"
        private const val KEY_REMEDIES = "show_home_remedies"
        private const val KEY_SAVE_HISTORY = "save_history"

        @Volatile
        private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                // applicationContext can be null while an Activity's base context is still being attached.
                instance ?: SettingsRepository(context.applicationContext ?: context).also { instance = it }
            }

        /** Wraps [base] so its resources use the saved language. Call from `Activity.attachBaseContext`. */
        fun withSavedLanguage(base: Context): Context {
            val language = get(base).settings.value.language
            val locale = Locale.forLanguageTag(language.tag)
            Locale.setDefault(locale)
            val config = Configuration(base.resources.configuration)
            config.setLocale(locale)
            return base.createConfigurationContext(config)
        }
    }
}

/** Opens the dialler with [number] filled in. The user still has to press call. */
fun dialNumber(context: Context, number: String) {
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
}
