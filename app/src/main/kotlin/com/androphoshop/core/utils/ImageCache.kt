package com.androphoshop.core.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.collection.LruCache
import coil.request.ImageRequest
import coil.request.SuccessResult

object ImageCache {
    private lateinit var cache: LruCache<String, Bitmap>

    fun initialize(context: Context) {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8 // Use 1/8th of the available memory for this cache.
        cache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size is measured in kilobytes, so divide by 1024.
                return bitmap.byteCount / 1024
            }
        }
    }

    fun put(key: String, bitmap: Bitmap) {
        if (!::cache.isInitialized) return
        cache.put(key, bitmap)
    }

    fun get(key: String): Bitmap? {
        if (!::cache.isInitialized) return null
        return cache.get(key)
    }

    // Coil integration hook (optional)
    fun fromCoil(request: ImageRequest, result: SuccessResult): Bitmap? {
        val drawable = result.drawable
        // Convert drawable to Bitmap if needed
        return null  // Placeholder
    }
}