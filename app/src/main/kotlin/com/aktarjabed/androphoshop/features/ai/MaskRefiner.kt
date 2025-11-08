package com.aktarjabed.androphoshop.features.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import kotlin.math.max
import kotlin.math.min

object MaskRefiner {
    fun featherEdges(mask: Bitmap, radius: Float): Bitmap {
        val bmp = mask.copy(Bitmap.Config.ALPHA_8, true)
        val w = bmp.width
        val h = bmp.height
        val pixels = ByteArray(w * h)
        val buffer = java.nio.ByteBuffer.wrap(pixels)
        bmp.copyPixelsToBuffer(buffer)

        fun clamp(v: Int, lo: Int, hi: Int) = max(lo, min(v, hi))
        val tmp = IntArray(w * h)
        val r = radius.toInt()

        // horizontal
        for (y in 0 until h) {
            var a = 0
            for (x in -r..r) {
                val xx = clamp(x, 0, w - 1)
                a += pixels[y * w + xx] & 0xFF
            }
            for (x in 0 until w) {
                val l = clamp(x - r - 1, 0, w - 1)
                val rdx = clamp(x + r, 0, w - 1)
                a += (pixels[y * w + rdx] & 0xFF) - (pixels[y * w + l] & 0xFF)
                val div = r * 2 + 1
                tmp[y * w + x] = (a / div)
            }
        }
        // vertical
        for (x in 0 until w) {
            var a = 0
            for (y in -r..r) {
                val yy = clamp(y, 0, h - 1)
                a += tmp[yy * w + x]
            }
            for (y in 0 until h) {
                val t = clamp(y - r - 1, 0, h - 1)
                val btm = clamp(y + r, 0, h - 1)
                a += tmp[btm * w + x] - tmp[t * w + x]
                val div = r * 2 + 1
                pixels[y * w + x] = (a / div).toByte()
            }
        }
        bmp.copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(pixels))
        return bmp
    }

    fun applyAlphaMask(source: Bitmap, mask: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(source, 0f, 0f, null)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null
        return result
    }
}