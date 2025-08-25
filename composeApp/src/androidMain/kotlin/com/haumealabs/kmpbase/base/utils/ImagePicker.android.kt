package com.haumealabs.aiphoto.utils

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.haumealabs.kmpbase.base.utils.ImagePicker

class AndroidImagePicker : ImagePicker {
    private var pickMediaLauncher: (() -> Unit)? = null
    private var legacyLauncher: (() -> Unit)? = null

    @Composable
    override fun createImagePickerLauncher(onImageSelected: (String) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Use the new photo picker API for Android 14+
            val pickMedia = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                uri?.let {
                    onImageSelected(it.toString())
                }
            }

            pickMediaLauncher = remember {
                { 
                    pickMedia.launch(
                        PickVisualMediaRequest(
                            mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                            // Remove any aspect ratio constraints to get full photos
                        )
                    ) 
                }
            }
        } else {
            // Use the legacy image picker for Android 13 and below
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        onImageSelected(uri.toString())
                    }
                }
            }

            legacyLauncher = remember {
                {
                    launcher.launch(
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    )
                }
            }
        }
    }

    override fun launchImagePicker() {
        pickMediaLauncher?.invoke() ?: legacyLauncher?.invoke()
    }
}