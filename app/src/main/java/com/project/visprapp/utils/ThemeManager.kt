package com.project.visprapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, THEME_DARK) ?: THEME_DARK
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value).apply()
            applyTheme(value)
        }

    fun applySavedTheme() {
        applyTheme(themeMode)
    }

    private fun applyTheme(mode: String) {
        when (mode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // THEME_DARK (Default)
        }
    }

    companion object {
        private const val PREFS_NAME = "vispr_theme_prefs"
        private const val KEY_THEME_MODE = "app_theme_mode"

        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_SYSTEM = "system"
    }
}
