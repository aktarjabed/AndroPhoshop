package com.androphoshop.data.repository

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import com.androphoshop.core.utils.Result
import com.androphoshop.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val context: Context,
    private val contentResolver: ContentResolver
) : ImageRepository {

    private val cache = mutableMapOf<String, Bitmap>()
    private var currentCacheSize = 0L
    private val maxCacheSize = 100 * 1024 * 1024L // 100MB

    override suspend fun loadBitmapFromUri(uri: Uri): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val cacheKey = uri.toString()
            cache[cacheKey]?.let { cachedBitmap ->
                return@withContext Result.Success(cachedBitmap)
            }

            contentResolver.openInputStream(uri)?.use { inputStream ->
                // First, decode with inJustDecodeBounds=true to check dimensions
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, 2048, 2048)
                options.inJustDecodeBounds = false

                // Decode bitmap with inSampleSize set
                contentResolver.openInputStream(uri)?.use { finalInputStream ->
                    val bitmap = BitmapFactory.decodeStream(finalInputStream, null, options)
                    if (bitmap != null) {
                        addToCache(cacheKey, bitmap)
                        return@withContext Result.Success(bitmap)
                    }
                }
            }
            Result.Error(Exception("Failed to load image from URI: $uri"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun saveBitmapToFile(bitmap: Bitmap, filename: String): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val imagesDir = File(context.getExternalFilesDir(null), "AndroPhoshop")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val imageFile = File(imagesDir, "$filename.jpg")
            FileOutputStream(imageFile).use { out ->
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)) {
                    Result.Success(Uri.fromFile(imageFile))
                } else {
                    Result.Error(Exception("Failed to compress bitmap"))
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun saveBitmapToGallery(bitmap: Bitmap, displayName: String): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AndroPhoshop")
            }

            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let { savedUri ->
                contentResolver.openOutputStream(savedUri)?.use { outputStream ->
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        Result.Success(savedUri)
                    } else {
                        Result.Error(Exception("Failed to save image to gallery"))
                    }
                } ?: Result.Error(Exception("Failed to open output stream"))
            } ?: Result.Error(Exception("Failed to create gallery entry"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun compressBitmap(bitmap: Bitmap, quality: Int): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val outputStream = ByteArrayOutputStream()
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)) {
                val compressedData = outputStream.toByteArray()
                val compressedBitmap = BitmapFactory.decodeByteArray(compressedData, 0, compressedData.size)
                Result.Success(compressedBitmap)
            } else {
                Result.Error(Exception("Failed to compress bitmap"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun clearCache() {
        cache.clear()
        currentCacheSize = 0L
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun addToCache(key: String, bitmap: Bitmap) {
        val bitmapSize = bitmap.byteCount

        while (currentCacheSize + bitmapSize > maxCacheSize && cache.isNotEmpty()) {
            val oldestKey = cache.keys.first()
            val oldestBitmap = cache.remove(oldestKey)
            oldestBitmap?.let {
                currentCacheSize -= it.byteCount
            }
        }

        cache[key] = bitmap
        currentCacheSize += bitmapSize
    }
}