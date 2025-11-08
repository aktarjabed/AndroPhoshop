package com.aktarjabed.androphoshop.features.editor.processors

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterProcessor @Inject constructor() {
    fun applyFilter(bitmap: Bitmap, type: String): Bitmap? {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        val matrix = when (type.lowercase()) {
            "grayscale" -> ColorMatrix().apply { setSaturation(0f) }
            "sepia" -> ColorMatrix().apply {
                setSaturation(0f)
                val sepiaMatrix = ColorMatrix().apply {
                    setScale(1f, .95f, .82f, 1f)
                }
                postConcat(sepiaMatrix)
            }
            else -> return bitmap
        }
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }
}