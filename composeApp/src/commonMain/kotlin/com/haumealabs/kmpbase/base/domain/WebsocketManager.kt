package com.haumealabs.kmpbase.base.domain

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class WebSocketManager(
    private val client: HttpClient,
    private val json: Json,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private var session: DefaultClientWebSocketSession? = null
    private val connectionStatus = MutableStateFlow(false)

    init {
        scope.launch { connectAndMaintain() }
    }

    val isConnected: StateFlow<Boolean> = connectionStatus.asStateFlow()

    private suspend fun connectAndMaintain() {
        while (scope.isActive) {
            try {
                println("WS Manager: Connecting...")
                client.webSocket(urlString = "wss://animaite.haumealabs.com:8080") { // Your WS endpoint
                    session = this
                    connectionStatus.value = true
                    println("WS Manager: Connected.")
                    listenIncoming() // Start listening loop
                }
            } catch (e: Exception) {
                println("WS Manager: Connection/Listen error: $e")
            } finally {
                session = null
                connectionStatus.value = false
                println("WS Manager: Disconnected. Retrying in 5s...")
                if (scope.isActive) delay(5000) // Wait before reconnecting
            }
        }
    }

    // Map for final results
    private val resultListeners = mutableMapOf<String, MutableSharedFlow<ServerResult>>()
    // Map specifically for Acknowledgment messages
    private val ackListeners = mutableMapOf<String, MutableSharedFlow<ServerAck>>()
    private val listenersMutex = Mutex()

    // ... (init, isConnected, connectAndMaintain)

    private suspend fun DefaultClientWebSocketSession.listenIncoming() {
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    println("WS Manager Received: $text")
                    var handled = false
                    // Try parsing as ServerAck first
                    try {
                        val ack = json.decodeFromString<ServerAck>(text)
                        listenersMutex.withLock {
                            ackListeners[ack.requestId]
                        }?.tryEmit(ack)
                        handled = true
                        // Optional: Remove ack listener once ack is received?
                        // listenersMutex.withLock { ackListeners.remove(ack.requestId) }
                        println("WS Manager: Handled as ServerAck for ${ack.requestId}")
                    } catch (e: SerializationException) {
                        // It's not a ServerAck, try ServerResult
                        // println("WS Manager: Not a ServerAck: ${e.message}") // Debug if needed
                    } catch (e: Exception) {
                        println("WS Manager: Error processing potential ServerAck: $e")
                    }

                    // If not handled as Ack, try parsing as ServerResult
                    if (!handled) {
                        try {
                            val result = json.decodeFromString<ServerResult>(text)
                            listenersMutex.withLock {
                                resultListeners[result.requestId]
                            }?.tryEmit(result)
                            println("WS Manager: Handled as ServerResult for ${result.requestId}")
                        } catch (e: SerializationException) {
                            println("WS Manager: Could not parse as ServerAck or ServerResult.")
                        } catch (e: Exception) {
                            println("WS Manager: Error processing potential ServerResult: $e")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("WS Manager: Error in listenIncoming: $e")
            // Don't rethrow here to allow the connectAndMaintain loop to retry
        }
    }


    // UseCase calls this - NOW accepts the single ClientRequest object
    suspend fun sendRequest(request: ClientRequest): String {
        val currentSession = session ?: throw IllegalStateException("WebSocket not connected")
        val requestId = request.requestId

        // Safely prepare listeners (both Ack and Result) before sending
        prepareListeners(requestId)

        // Encode the whole request object to JSON
        val requestJson = json.encodeToString(ClientRequest.serializer(), request)

        // Send as a single text frame
        currentSession.send(Frame.Text(requestJson))
        println("WS Manager: Sent request $requestId (Action: ${request.action})")

        // Returns immediately after sending
        return requestId
    }

    // ViewModel calls this
    suspend fun listenForResult(requestId: String): Flow<ServerResult> {
        val flow = listenersMutex.withLock {
            resultListeners.getOrPut(requestId) {
                createSharedFlow()
            }
        }
        return flow.asSharedFlow().onCompletion {
            // Optional: Consider cleanup logic here, potentially under mutex lock again
            // listenersMutex.withLock { resultListeners.remove(requestId) } // If needed
            println("Stopped listening for Result $requestId")
        }
    }

    // UseCase can call this
    suspend fun listenForAck(requestId: String): Flow<ServerAck> {
        val flow = listenersMutex.withLock {
            ackListeners.getOrPut(requestId) {
                createSharedFlow()
            }
        }
        return flow.asSharedFlow().onCompletion {
            // Optional: Cleanup Ack listener here if not done in listenIncoming
            listenersMutex.withLock { ackListeners.remove(requestId) } // Example cleanup
            println("Stopped listening for Ack $requestId")
        }
    }

    // Prepare both listeners
    private suspend fun prepareListeners(requestId: String) {
        listenersMutex.withLock {
            ackListeners.getOrPut(requestId) { createSharedFlow() }
            resultListeners.getOrPut(requestId) { createSharedFlow() }
        }
    }

    // Helper to create the SharedFlow with common settings
    private fun <T> createSharedFlow(): MutableSharedFlow<T> {
        return MutableSharedFlow(replay = 1, onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST)
    }

    // Cleanup function if needed
    suspend fun cleanupListeners(requestId: String) {
        listenersMutex.withLock {
            ackListeners.remove(requestId)
            resultListeners.remove(requestId)
        }
        println("Cleaned up listeners for $requestId")
    }

    // ... (other potential cleanup logic)
}