package com.haumealabs.kmpbase.base.utils

import androidx.compose.runtime.Composable

interface ImagePicker {
    @Composable
    fun createImagePickerLauncher(onImageSelected: (String) -> Unit)
    
    fun launchImagePicker()
}