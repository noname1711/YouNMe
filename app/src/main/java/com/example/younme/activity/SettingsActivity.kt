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

        // Kiểm tra cài đặt chế độ tối từ SharedPreferences
        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

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
                // Lưu cài đặt chế độ tối vào SharedPreferences
                val sharedPreferences = requireActivity().getSharedPreferences("settings", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("dark_mode", isDarkMode)
                editor.apply()

                AppCompatDelegate.setDefaultNightMode(
                    if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
                updateDarkModeTitle(isDarkMode)
                requireActivity().recreate()  // reset thay đổi
                true
            }
            // Đổi title tùy theo chế độ hiện tại
            updateDarkModeTitle(darkModePreference.isChecked)
        }

        private fun setLocale(language: String) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
            activity?.recreate()  // load lại ngôn ngữ
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
