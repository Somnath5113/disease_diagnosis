package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.settings.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LanguageAssetsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val assets = context.assets.list("").orEmpty().toSet()

    @Test
    fun `clinicalContent matches the translated question trees that are bundled`() {
        for (language in AppLanguage.entries) {
            val asset = if (language == AppLanguage.ENGLISH) "triage_tree.json" else "triage_tree_${language.tag}.json"
            assertEquals(
                "${language.name}: clinicalContent must be true exactly when $asset is bundled",
                language.clinicalContent,
                asset in assets
            )
        }
    }

    @Test
    fun `every language has interface strings`() {
        // R.string.* is merged from values-<tag>/strings.xml; a missing folder would show English silently.
        for (language in AppLanguage.entries.filter { it != AppLanguage.ENGLISH }) {
            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocale(java.util.Locale.forLanguageTag(language.tag))
            val localized = context.createConfigurationContext(config)
            val english = context.getString(R.string.settings_title)
            assertTrue(
                "${language.name}: settings_title is not translated",
                localized.getString(R.string.settings_title) != english
            )
        }
    }
}
