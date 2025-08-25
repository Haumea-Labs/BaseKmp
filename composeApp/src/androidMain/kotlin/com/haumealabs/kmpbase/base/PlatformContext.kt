package com.haumealabs.kmpbase.base

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.haumealabs.aiphoto.BuildConfig
import kotlin.apply


actual class PlatformContext private constructor(internal val context: Context, internal val androidActivity: Activity) {
    actual companion object {
        private lateinit var androidContext: Context
        private lateinit var androidActivity: Activity

        fun initialize(context: Context, activity: Activity) {
            androidContext = context.applicationContext
            androidActivity = activity
        }

        actual fun getContext(): Any = androidContext

        actual fun getActivity(): Any = androidActivity

        actual fun getPlatformName(): String = "android"

        actual fun getRevenueCatApiKey(): String? = "goog_ilXokCFnRveRnbRRiukJsRmeElB"

        actual fun isDev(): Boolean = BuildConfig.DEBUG
    }
}

@Composable
actual fun ConfigureEdgeToEdge() {
    val view = LocalView.current
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val statusBarColor = Color.White

    SideEffect {
        val window = (context as Activity).window

        // Edge-to-edge configuration
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Status bar configuration
        window.statusBarColor = statusBarColor.toArgb()
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = statusBarColor.luminance() > 0.5f
        }

        // Optional: Navigation bar customization
        window.navigationBarColor = Color.Transparent.toArgb()
    }
}