package com.haumealabs.aiphoto.utils

import android.app.Activity
import android.view.Window
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

class AndroidStatusBarColor(private val activity: Activity) : StatusBarColor {
    override fun setStatusBarColor(color: Color) {
        val window: Window = activity.window
        window.statusBarColor = color.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = color.luminance() > 0.5
        }
    }
} 