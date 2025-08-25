package com.haumealabs.kmpbase.base.storage

import android.content.Context
import androidx.core.content.edit
import com.haumealabs.kmpbase.base.PlatformContext


actual class Storage {
    private val sharedPreferences by lazy {
        (PlatformContext.getContext() as Context).getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    }

    actual fun saveString(label: String, value: String) {
        sharedPreferences.edit { putString(label, value) }
    }

    actual fun loadString(label: String): String {
        return sharedPreferences.getString(label, "") ?: ""
    }

    actual fun saveInt(label: String, value: Int) {
        sharedPreferences.edit { putInt(label, value) }
    }

    actual fun loadInt(label: String): Int {
        return sharedPreferences.getInt(label, 0)
    }

    actual fun saveBoolean(label: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(label, value) }
    }

    actual fun loadBoolean(label: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(label, defaultValue)
    }

}