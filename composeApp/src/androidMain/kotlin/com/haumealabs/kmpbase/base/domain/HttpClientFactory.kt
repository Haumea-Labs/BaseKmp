package com.haumealabs.kmpbase.base.domain

import io.ktor.client.* // HttpClient import
import io.ktor.client.engine.okhttp.OkHttp // OkHttp engine import
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation // ContentNegotiation import
import io.ktor.client.plugins.websocket.WebSockets // WebSockets import
import io.ktor.serialization.kotlinx.json.json // Json serialization import
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * Actual implementation for Android, creating an HttpClient using the OkHttp engine.
 */
actual fun createHttpClient(): HttpClient {
    return HttpClient(OkHttp) {
        // Configure engine specific settings
        engine {
            config {
                // Example: Adjust timeouts if needed
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
                // Allow websockets to run for longer
                pingInterval(20, TimeUnit.SECONDS)
            }
        }

        // Install features (should match common configuration)
        install(WebSockets) {
            // You can configure WebSocket specific settings here if needed
        }

        install(ContentNegotiation) {
            json(Json { // Re-create or inject the common Json configuration
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        // Add other common plugins like Logging if desired
    }
}