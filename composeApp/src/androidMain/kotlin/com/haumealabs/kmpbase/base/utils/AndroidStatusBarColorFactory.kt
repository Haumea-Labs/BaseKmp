package com.haumealabs.aiphoto.utils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual object StatusBarColorFactory {
    @Composable
    actual fun createStatusBarColor(): StatusBarColor {
        val context = LocalContext.current
        return AndroidStatusBarColor(context as Activity)
    }
} 