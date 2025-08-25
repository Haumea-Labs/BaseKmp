package com.haumealabs.kmpbase.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageResponse(
    val message: String,
    val generatedImage: GeneratedImage,
    val s3Location: String
)

@Serializable
data class GeneratedImage(
    val mimeType: String,
    val data: String
) 