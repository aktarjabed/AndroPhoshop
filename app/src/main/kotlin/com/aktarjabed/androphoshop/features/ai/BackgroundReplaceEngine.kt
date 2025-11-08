package com.aktarjabed.androphoshop.features.ai

import android.graphics.*
import android.os.Build
import kotlin.math.max
import kotlin.math.min

/**
 * High-quality background replacement using an RGBA foreground (subject cutout) and:
 *  - solid color
 *  - external image (fit/fill/crop)
 *  - auto-generated blurred background from the original
 * Optional soft-edge feather & subtle contact shadow for realism.
 */
class BackgroundReplaceEngine {

    enum class Mode { COLOR, IMAGE_FIT, IMAGE_FILL, AUTO_BLUR }

    data class Params(
        val mode: Mode = Mode.AUTO_BLUR,
        val color: Int = Color.WHITE,
        val featherRadius: Float = 6f,
        val addShadow: Boolean = true,
        val shadowOpacity: Float = 0.22f,   // 0..1
        val shadowSizePx: Float = 24f,      // blur radius
        val shadowOffsetYPx: Float = 12f,   // down offset
    )

    /**
     * @param original   original (used for AUTO_BLUR generation)
     * @param cutout     subject bitmap with transparent background (ARGB_8888, premultiplied)
     * @param background optional background image (used for IMAGE_FIT / IMAGE_FILL)
     * @param params     controls for mode/feather/shadow
     */
    fun compose(
        original: Bitmap,
        cutout: Bitmap,
        background: Bitmap?,
        params: Params
    ): Bitmap {
        val w = original.width
        val h = original.height
        val canvasBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(canvasBmp)

        // 1) Render background per mode
        when (params.mode) {
            Mode.COLOR -> {
                canvas.drawColor(params.color)
            }
            Mode.AUTO_BLUR -> {
                // Fast large blur from original
                val bg = fastBlur(original, 20f)
                canvas.drawBitmap(bg, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
            }
            Mode.IMAGE_FIT -> {
                background?.let { drawFit(it, canvas, w, h) } ?: canvas.drawColor(Color.DKGRAY)
            }
            Mode.IMAGE_FILL -> {
                background?.let { drawFill(it, canvas, w, h) } ?: canvas.drawColor(Color.DKGRAY)
            }
        }

        // 2) Optional subtle shadow under subject
        if (params.addShadow) {
            drawShadow(canvas, cutout, params.shadowSizePx, params.shadowOpacity, params.shadowOffsetYPx)
        }

        // 3) Feather cutout edges slightly then composite
        val alphaOnly = cutout.extractAlpha()
        val softAlpha = featherEdges(alphaOnly, params.featherRadius)
        val refined = applyAlphaMask(cutout, softAlpha) // just to ensure soft edge on subject

        canvas.drawBitmap(refined, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
        return canvasBmp
    }

    private fun drawFit(src: Bitmap, canvas: Canvas, dstW: Int, dstH: Int) {
        val scale = min(dstW.toFloat() / src.width, dstH.toFloat() / src.height)
        val w = (src.width * scale)
        val h = (src.height * scale)
        val left = (dstW - w) / 2f
        val top = (dstH - h) / 2f
        val m = Matrix().apply { postScale(scale, scale); postTranslate(left, top) }
        canvas.drawBitmap(src, m, Paint(Paint.ANTI_ALIAS_FLAG))
    }

    private fun drawFill(src: Bitmap, canvas: Canvas, dstW: Int, dstH: Int) {
        val scale = max(dstW.toFloat() / src.width, dstH.toFloat() / src.height)
        val w = (src.width * scale)
        val h = (src.height * scale)
        val left = (dstW - w) / 2f
        val top = (dstH - h) / 2f
        val m = Matrix().apply { postScale(scale, scale); postTranslate(left, top) }
        canvas.drawBitmap(src, m, Paint(Paint.ANTI_ALIAS_FLAG))
    }

    private fun drawShadow(canvas: Canvas, subject: Bitmap, sizePx: Float, opacity: Float, offsetY: Float) {
        // Build a blurred alpha silhouette as a soft shadow under subject
        val alpha = subject.extractAlpha()
        val shadowBmp = if (Build.VERSION.SDK_INT >= 31) {
            val out = Bitmap.createBitmap(alpha.width, alpha.height, Bitmap.Config.ALPHA_8)
            val c = Canvas(out)
            val p = Paint().apply {
                renderEffect = RenderEffect.createBlurEffect(sizePx, sizePx, Shader.TileMode.CLAMP)
                alpha = (opacity * 255).toInt().coerceIn(0, 255)
            }
            c.drawBitmap(alpha, 0f, 0f, p)
            out
        } else {
            // Fallback: simple box blur via MaskRefiner feather, then tone alpha
            val out = featherEdges(alpha, sizePx)
            out
        }
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = (opacity * 255).toInt().coerceIn(0, 255)
        }
        val m = Matrix().apply { postTranslate(0f, offsetY) }
        canvas.drawBitmap(shadowBmp, m, p)
    }

    // Fast blur helper (RenderEffect on S+, else simple box blur)
    private fun fastBlur(src: Bitmap, radius: Float): Bitmap {
        return if (Build.VERSION.SDK_INT >= 31) {
            val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
            val c = Canvas(out)
            val p = Paint().apply {
                renderEffect = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
            }
            c.drawBitmap(src, 0f, 0f, p)
            out
        } else {
            // reuse FilterProcessor's boxBlur-like idea (small radius for perf)
            BoxBlur.blur(src, 3)
        }
    }

    // Minimal box blur port for pre-S; small radius for speed
    private object BoxBlur {
        fun blur(src: Bitmap, radius: Int): Bitmap {
            val bmp = src.copy(Bitmap.Config.ARGB_8888, true)
            val w = bmp.width; val h = bmp.height
            val pixels = IntArray(w * h)
            bmp.getPixels(pixels, 0, w, 0, 0, w, h)

            fun clamp(v: Int, lo: Int, hi: Int) = max(lo, min(v, hi))
            val tmp = IntArray(w * h)

            // horizontal
            for (y in 0 until h) {
                var a=0; var r=0; var g=0; var b=0
                for (x in -radius..radius) {
                    val xx = clamp(x, 0, w-1)
                    val c = pixels[y*w + xx]
                    a += c ushr 24 and 0xFF
                    r += c shr 16 and 0xFF
                    g += c shr 8 and 0xFF
                    b += c and 0xFF
                }
                for (x in 0 until w) {
                    val l = clamp(x-radius-1, 0, w-1)
                    val rdx = clamp(x+radius, 0, w-1)
                    val cl = pixels[y*w + l]
                    val cr = pixels[y*w + rdx]
                    a += (cr ushr 24 and 0xFF) - (cl ushr 24 and 0xFF)
                    r += (cr shr 16 and 0xFF) - (cl shr 16 and 0xFF)
                    g += (cr shr 8 and 0xFF) - (cl shr 8 and 0xFF)
                    b += (cr and 0xFF) - (cl and 0xFF)
                    val div = radius*2+1
                    tmp[y*w + x] = (a/div shl 24) or (r/div shl 16) or (g/div shl 8) or (b/div)
                }
            }
            // vertical
            for (x in 0 until w) {
                var a=0; var r=0; var g=0; var b=0
                for (y in -radius..radius) {
                    val yy = clamp(y, 0, h-1)
                    val c = tmp[yy*w + x]
                    a += c ushr 24 and 0xFF
                    r += c shr 16 and 0xFF
                    g += c shr 8 and 0xFF
                    b += c and 0xFF
                }
                for (y in 0 until h) {
                    val t = clamp(y-radius-1, 0, h-1)
                    val btm = clamp(y+radius, 0, h-1)
                    val cb = tmp[btm*w + x]
                    a += (cb ushr 24 and 0xFF) - (ct ushr 24 and 0xFF)
                    r += (cb shr 16 and 0xFF) - (ct shr 16 and 0xFF)
                    g += (cb shr 8 and 0xFF) - (ct shr 8 and 0xFF)
                    b += (cb and 0xFF) - (ct and 0xFF)
                    val div = radius*2+1
                    pixels[y*w + x] = (a/div shl 24) or (r/div shl 16) or (g/div shl 8) or (b/div)
                }
            }
            bmp.setPixels(pixels, 0, w, 0, 0, w, h)
            return bmp
        }
    }
}