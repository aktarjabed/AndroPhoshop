package com.aktarjabed.androphoshop.features.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint

object BitmapUtils {
    fun copy(src: Bitmap, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, config)
        Canvas(out).drawBitmap(src, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
        return out
    }

    fun scale(src: Bitmap, w: Int, h: Int): Bitmap =
        if (src.width == w && src.height == h) src else
            Bitmap.createScaledBitmap(src, w, h, true)

    fun rotate(src: Bitmap, degrees: Float): Bitmap {
        val m = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }
}