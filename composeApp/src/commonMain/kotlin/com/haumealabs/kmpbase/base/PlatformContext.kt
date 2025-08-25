package com.haumealabs.kmpbase.base

import androidx.compose.runtime.Composable

expect class PlatformContext {
    companion object {
        fun getContext(): Any
        fun getActivity(): Any
        fun getPlatformName(): String
        fun getRevenueCatApiKey(): String?
        fun isDev(): Boolean
    }
}

@Composable
expect fun ConfigureEdgeToEdge()

