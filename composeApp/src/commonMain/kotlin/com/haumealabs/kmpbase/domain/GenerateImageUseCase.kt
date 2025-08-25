package com.haumealabs.aiphoto.domain

import com.haumealabs.aiphoto.architecture.domain.result.UseCaseResult
import com.haumealabs.aiphoto.architecture.domain.usecase.SuspendedUseCase
import com.haumealabs.aiphoto.base.PlatformContext
import com.haumealabs.kmpbase.model.ClientRequest
import com.haumealabs.aiphoto.utils.Singleton.Companion.analyticsHelper
import com.haumealabs.kmpbase.base.domain.FileOperations
import com.haumealabs.kmpbase.base.domain.WebSocketManager
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random

data class GenerateImageParams(
    val style: String,
    val imageUri: String
)

class GenerateImageUseCase(
    private val fileOperations: FileOperations,
    private val webSocketManager: WebSocketManager
) : SuspendedUseCase<GenerateImageParams, String>() {

    // Define a timeout for waiting for the Ack message (e.g., 10 seconds)
    private val ackTimeoutMillis = 10_000L

    override suspend fun run(params: GenerateImageParams): UseCaseResult<String> {
        return try {
            // 1. Check file existence
            if (!fileOperations.fileExists(params.imageUri)) {
                return UseCaseResult.Error(Exception("File does not exist: ${params.imageUri}"))
            }

            // 2. Read file bytes
            var fileBytes = fileOperations.getFileBytes(params.imageUri)
                ?: return UseCaseResult.Error(Exception("Failed to read file bytes"))

            // 3. Convert to WebP if necessary (server expects WebP)
            fileBytes = fileOperations.convertToWebP(fileBytes)
                ?: return UseCaseResult.Error(Exception("Failed to convert image to PNG"))

            // 4. Encode bytes to Base64
            val base64Image = withContext(Dispatchers.Default) {
                fileBytes.encodeBase64()
            }

            // 5. Generate a unique request ID (simple example)
            val requestId = "req_${Random.nextLong()}"

            // 6. Construct the request object
            val request = ClientRequest(
                requestId = requestId,
                action = "generateImage",
                style = params.style,
                base64Image = base64Image
            )

            // 7. Send via WebSocketManager
            // Note: sendRequest now prepares listeners internally
            webSocketManager.sendRequest(request)
            analyticsHelper?.trackEvent("generation_request_sent",
                listOf(
                    "style" to params.style,
                    "platform" to PlatformContext.getPlatformName()
                )
            )

            // 8. Wait for the ServerAck
            val ack = withTimeoutOrNull(ackTimeoutMillis) {
                webSocketManager.listenForAck(requestId).firstOrNull()
            }

            // 9. Check if Ack was receivedsudo
            if (ack == null) {
                analyticsHelper?.trackEvent("generation_ack_timeout")
                // Optionally cleanup listeners if timeout occurs
                // webSocketManager.cleanupListeners(requestId) // Consider if needed here
                return UseCaseResult.Error(Exception("Did not receive server acknowledgment within ${ackTimeoutMillis}ms"))
            }

            println("Received Ack: $ack") // Log the received Ack
            analyticsHelper?.trackEvent("generation_ack_received")


            // 10. Return the request ID for the ViewModel to listen for the final result
            UseCaseResult.Success(requestId)

        } catch (e: Exception) {
            analyticsHelper?.trackEvent("generation_request_failed", listOf("error" to (e.message ?: "Unknown")))
            if (e is IllegalStateException && e.message == "WebSocket not connected") {
                UseCaseResult.Error(Exception("Cannot process image: Connection unavailable.", e))
            } else {
                // Ensure we pass the original exception for better debugging
                UseCaseResult.Error(Exception("Error during image generation process: ${e.message}", e))
            }
        }
    }
}