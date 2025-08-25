package com.haumealabs.kmpbase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.haumealabs.aiphoto.utils.AndroidImagePicker
import com.haumealabs.kmpbase.base.PlatformContext
import com.haumealabs.kmpbase.base.rating.RatingManager
import com.haumealabs.kmpbase.base.domain.AndroidFileOperations

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PlatformContext.initialize(applicationContext, this)

        setContent {
            setContent {
                val fileOperations = AndroidFileOperations(applicationContext)
                val ratingManager = RatingManager(this, lifecycleScope)
                val imagePicker = AndroidImagePicker()

                App(fileOperations, imagePicker, ratingManager)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}