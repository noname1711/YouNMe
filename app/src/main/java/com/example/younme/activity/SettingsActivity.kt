package com.example.younme.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.example.younme.R
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var darkModePreference: SwitchPreference

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.setting, rootKey)

            val languagePreference: ListPreference? = findPreference("language_preference")
            languagePreference?.setOnPreferenceChangeListener { _, newValue ->
                setLocale(newValue as String)
                true
            }

            darkModePreference = findPreference("dark_mode_preference")!!
            darkModePreference.setOnPreferenceChangeListener { _, newValue ->
                val isDarkMode = newValue as Boolean
                AppCompatDelegate.setDefaultNightMode(
                    if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
                updateDarkModeTitle(isDarkMode)
                requireActivity().recreate()  // Recreate activity to apply changes
                true
            }

            // Initialize the title based on the current state
            updateDarkModeTitle(darkModePreference.isChecked)
        }

        private fun setLocale(language: String) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
            activity?.recreate()  // Recreate activity to apply new language
        }

        private fun updateDarkModeTitle(isDarkMode: Boolean) {
            val title = if (isDarkMode) {
                getString(R.string.disable_dark_mode_title)
            } else {
                getString(R.string.enable_dark_mode_title)
            }
            darkModePreference.title = title
        }
    }
}
