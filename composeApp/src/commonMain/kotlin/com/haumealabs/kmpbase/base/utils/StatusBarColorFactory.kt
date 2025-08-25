package com.haumealabs.aiphoto.utils

import androidx.compose.runtime.Composable

expect object StatusBarColorFactory {
    @Composable
    fun createStatusBarColor(): StatusBarColor
} 