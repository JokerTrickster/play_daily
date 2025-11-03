package com.dailymemo.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Image compression utility to optimize image sizes before upload
 * Target: Images should be < 500KB each
 */
@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MAX_IMAGE_SIZE_KB = 500
        private const val MAX_DIMENSION = 1920
        private const val INITIAL_QUALITY = 90
        private const val MIN_QUALITY = 10
        private const val QUALITY_STEP = 10
    }

    /**
     * Compress image from URI to meet size constraints
     * @param uri Source image URI
     * @param maxSizeKb Maximum file size in KB (default 500KB)
     * @return Compressed image bytes
     */
    suspend fun compressImage(
        uri: Uri,
        maxSizeKb: Int = MAX_IMAGE_SIZE_KB
    ): ByteArray = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        // Decode original bitmap
        val originalBitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw IllegalArgumentException("Cannot decode image from URI")

        try {
            // Scale down if necessary
            val scaledBitmap = scaleDownBitmap(originalBitmap)

            // Compress to target size
            val compressedBytes = compressToTargetSize(
                bitmap = scaledBitmap,
                maxSizeKb = maxSizeKb
            )

            // Clean up bitmaps
            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()

            compressedBytes
        } catch (e: Exception) {
            // Cleanup on error
            originalBitmap.recycle()
            throw e
        }
    }

    /**
     * Scale down bitmap if dimensions exceed max
     */
    private fun scaleDownBitmap(bitmap: Bitmap): Bitmap {
        if (bitmap.width <= MAX_DIMENSION && bitmap.height <= MAX_DIMENSION) {
            return bitmap
        }

        val scale = minOf(
            MAX_DIMENSION.toFloat() / bitmap.width,
            MAX_DIMENSION.toFloat() / bitmap.height
        )

        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Compress bitmap to target size by adjusting quality
     */
    private fun compressToTargetSize(
        bitmap: Bitmap,
        maxSizeKb: Int
    ): ByteArray {
        var quality = INITIAL_QUALITY
        var compressedBytes: ByteArray

        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedBytes = outputStream.toByteArray()
            outputStream.close()

            val sizeKb = compressedBytes.size / 1024

            if (sizeKb <= maxSizeKb || quality <= MIN_QUALITY) {
                break
            }

            quality -= QUALITY_STEP
        } while (true)

        return compressedBytes
    }

    /**
     * Get compressed image size in KB
     */
    fun getImageSizeKb(imageBytes: ByteArray): Int {
        return imageBytes.size / 1024
    }
}
