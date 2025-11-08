package com.aktarjabed.androphoshop.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import com.aktarjabed.androphoshop.core.utils.Result

interface ImageRepository {
    suspend fun loadBitmapFromUri(uri: Uri): Result<Bitmap>
    suspend fun saveBitmapToFile(bitmap: Bitmap, filename: String): Result<Uri>
    suspend fun saveBitmapToGallery(bitmap: Bitmap, displayName: String): Result<Uri>
    suspend fun compressBitmap(bitmap: Bitmap, quality: Int): Result<Bitmap>
    fun clearCache()
}