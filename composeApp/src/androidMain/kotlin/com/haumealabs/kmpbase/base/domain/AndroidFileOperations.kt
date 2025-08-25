package com.haumealabs.kmpbase.base.domain

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidFileOperations(private val context: Context) : FileOperations {
    override suspend fun getFileBytes(uri: String): ByteArray? {
        return try {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            println("Error reading file bytes for URI $uri: ${e.message}")
            null
        }
    }

    override suspend fun getFileExtension(uri: String): String {
        val mimeType = context.contentResolver.getType(Uri.parse(uri))
        return if (mimeType != null) {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "png"
        } else {
            uri.substringAfterLast('.', "png")
        }
    }

    override suspend fun fileExists(uri: String): Boolean {
        return try {
            context.contentResolver.query(Uri.parse(uri), null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
            } ?: false
        } catch (e: Exception) {
            println("Error checking existence for URI $uri: ${e.message}")
            false
        }
    }

    override suspend fun saveBase64(base64String: String, fileName: String): String = withContext(Dispatchers.IO) {
        try {
            val pureBase64 = if (base64String.startsWith("data:image")) {
                base64String.substringAfter(',')
            } else {
                base64String
            }

            val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)

            val bitmap: Bitmap? = decodeBitmapFromBytes(imageBytes)

            if (bitmap == null) {
                println("Error: Failed to decode Base64 string into Bitmap for $fileName")
                return@withContext ""
            }

            val cacheDir = context.cacheDir ?: run {
                println("Error: Cache directory is null.")
                bitmap.recycle()
                return@withContext ""
            }
            val cacheFile = File(cacheDir, fileName)

            cacheFile.outputStream().use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            bitmap.recycle()

            try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cacheFile
                ).toString()
            } catch (e: IllegalArgumentException) {
                println("Error creating FileProvider URI for $fileName: ${e.message}")
                cacheFile.delete()
                ""
            }

        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun decodeBitmapFromBytes(imageBytes: ByteArray): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(ByteBuffer.wrap(imageBytes))
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                }
            } else {
                @Suppress("DEPRECATION")
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.copy(Bitmap.Config.ARGB_8888, true)
            }
        } catch (e: Exception) {
            println("Bitmap decoding failed: ${e.message}")
            null
        }
    }

    override suspend fun saveToGallery(uri: String, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(Uri.parse(uri)) ?: run {
                println("Error: Could not open input stream for URI $uri")
                return@withContext false
            }
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                println("Error: Failed to decode bitmap from stream for URI $uri")
                return@withContext false
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                } else {
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val file = File(picturesDir, fileName)
                    put(MediaStore.MediaColumns.DATA, file.absolutePath)
                }
            }

            val resolver = context.contentResolver
            var galleryUri: Uri? = null
            try {
                galleryUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (galleryUri == null) {
                    println("Error: MediaStore insert failed for $fileName")
                    bitmap.recycle()
                    return@withContext false
                }

                resolver.openOutputStream(galleryUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                } ?: run {
                    println("Error: Could not open output stream for gallery URI $galleryUri")
                    resolver.delete(galleryUri, null, null)
                    bitmap.recycle()
                    return@withContext false
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(galleryUri, contentValues, null, null)
                }

                bitmap.recycle()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                if (galleryUri != null) {
                    try { resolver.delete(galleryUri, null, null) } catch (delEx: Exception) { /* ignore cleanup error */ }
                }
                bitmap.recycle()
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun convertToPng(imageBytes: ByteArray): ByteArray? = withContext(Dispatchers.IO) {
        val originalBitmap: Bitmap? = decodeBitmapFromBytes(imageBytes)

        if (originalBitmap == null) {
            println("Error: Failed to decode bitmap in convertToPng")
            return@withContext null
        }

        try {
            val maxDimension = 1920
            val scale: Float = when {
                originalBitmap.width <= maxDimension && originalBitmap.height <= maxDimension -> 1f
                originalBitmap.width > originalBitmap.height -> maxDimension.toFloat() / originalBitmap.width
                else -> maxDimension.toFloat() / originalBitmap.height
            }

            val bitmapToCompress = if (scale < 1f) {
                val newWidth = (originalBitmap.width * scale).toInt().coerceAtLeast(1)
                val newHeight = (originalBitmap.height * scale).toInt().coerceAtLeast(1)
                try {
                    Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                } catch (oom: OutOfMemoryError) {
                    println("OOM Error during scaling in convertToPng. Trying smaller scale.")
                    null
                } catch (e: Exception) {
                    println("Error during scaling in convertToPng: ${e.message}")
                    null
                }
            } else {
                originalBitmap
            }

            if (bitmapToCompress == null) {
                originalBitmap.recycle()
                return@withContext null
            }

            val outputStream = ByteArrayOutputStream()
            bitmapToCompress.compress(Bitmap.CompressFormat.PNG, 95, outputStream)

            if (bitmapToCompress != originalBitmap) {
                bitmapToCompress.recycle()
            }
            originalBitmap.recycle()

            outputStream.toByteArray()

        } catch (e: Exception) {
            e.printStackTrace()
            originalBitmap.recycle()
            null
        }
    }

    override suspend fun convertToWebP(imageBytes: ByteArray): ByteArray? = withContext(Dispatchers.IO) {
        val originalBitmap: Bitmap? = decodeBitmapFromBytes(imageBytes)

        if (originalBitmap == null) {
            println("Error: Failed to decode bitmap in convertToWebP")
            return@withContext null
        }

        try {
            // No resizing here, just format conversion. Add resizing logic if needed.
            val outputStream = ByteArrayOutputStream()
            val quality = 80 // Adjust quality (0-100) for desired compression/size

            // Choose appropriate WebP format based on API level
            val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30+
                originalBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, outputStream)
            } else {
                // Use deprecated WEBP format for older APIs (14-29)
                @Suppress("DEPRECATION")
                originalBitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
            }

            originalBitmap.recycle() // Recycle the bitmap after compression

            if (success) {
                outputStream.toByteArray()
            } else {
                println("Error: Failed to compress bitmap to WebP")
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            originalBitmap.recycle() // Ensure recycle on error
            null
        }
    }

    override suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            try {
                val cacheDir = context.cacheDir ?: return@withContext
                val files = cacheDir.listFiles()
                if (files != null) {
                    for (file in files) {
                        try {
                            if (file.isFile) {
                                if (!file.delete()) {
                                    println("Warning: Failed to delete cache file: ${file.absolutePath}")
                                }
                            } else if (file.isDirectory) {
                                if (!file.deleteRecursively()) {
                                    println("Warning: Failed to delete cache directory: ${file.absolutePath}")
                                }
                            }
                        } catch (e: SecurityException) {
                            println("SecurityException deleting cache item: ${file.absolutePath} - ${e.message}")
                        }
                    }
                }
                println("App cache cleared.")
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error clearing cache: ${e.message}")
            }
        }
    }
}