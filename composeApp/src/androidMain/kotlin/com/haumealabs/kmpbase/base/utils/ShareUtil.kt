package com.haumealabs.aiphoto.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.haumealabs.kmpbase.base.PlatformContext

actual class ShareUtil {
    actual fun shareImage(uri: String) {
        val context = PlatformContext.getContext() as Context
        val contentUri = uri.toUri()
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share Image")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
} 