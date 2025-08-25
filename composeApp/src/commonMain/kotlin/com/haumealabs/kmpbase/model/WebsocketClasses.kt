package com.haumealabs.kmpbase.model

import kotlinx.serialization.Serializable

@Serializable
data class ClientRequest(
    val requestId: String, // Unique ID for this specific request
    val action: String,    // What action to perform (e.g., "generateImage")
    val style: String,
    val base64Image: String
)

// Keep ServerResult as previously defined, assuming it still fits
@Serializable
data class ServerResult(
    val requestId: String,
    val imageUrl: String? = null,
    val error: String? = null
)

// Optional: Add an Ack message if the server sends one
@Serializable
data class ServerAck(
    val requestId: String,
    val status: String,
    val jobId: String
)