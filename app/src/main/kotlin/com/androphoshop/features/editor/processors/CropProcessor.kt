package com.androphoshop.features.editor.processors

import android.graphics.Bitmap
import android.graphics.Rect
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropProcessor @Inject constructor() {
    fun crop(bitmap: Bitmap, rect: Rect): Bitmap {
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
    }
}