package com.haumealabs.kmpbase.base.domain

interface FileOperations {
    suspend fun getFileBytes(uri: String): ByteArray?
    suspend fun getFileExtension(uri: String): String
    suspend fun fileExists(uri: String): Boolean
    suspend fun saveBase64(base64String: String, fileName: String): String
    suspend fun saveToGallery(uri: String, fileName: String): Boolean
    suspend fun convertToPng(imageBytes: ByteArray): ByteArray?
    suspend fun convertToWebP(imageBytes: ByteArray): ByteArray?
    suspend fun clearCache()
} 