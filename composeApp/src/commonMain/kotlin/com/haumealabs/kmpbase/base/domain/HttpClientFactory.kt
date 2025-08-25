package com.haumealabs.kmpbase.base.domain

import io.ktor.client.* // HttpClient needs to be imported

/**
 * Expect declaration for a function that creates a platform-specific HttpClient.
 */
expect fun createHttpClient(): HttpClient