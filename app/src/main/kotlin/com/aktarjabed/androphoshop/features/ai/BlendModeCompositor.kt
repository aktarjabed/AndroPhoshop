package com.aktarjabed.androphoshop.features.ai

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * CPU per-pixel blend modes for subject-over-background compositing.
 * Optimized enough for mobile when used once per apply; keep preview sizes moderate.
 */
object BlendModeCompositor {

    enum class Mode { NORMAL, MULTIPLY, SCREEN, OVERLAY }

    fun composite(bg: Bitmap, fg: Bitmap, mode: Mode = Mode.NORMAL): Bitmap {
        require(bg.width == fg.width && bg.height == fg.height) { "Size mismatch" }
        val w = bg.width; val h = bg.height
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val bp = IntArray(w * h)
        val fp = IntArray(w * h)
        bg.getPixels(bp, 0, w, 0, 0, w, h)
        fg.getPixels(fp, 0, w, 0, 0, w, h)

        for (i in 0 until bp.size) {
            val b = bp[i]
            val f = fp[i]
            val fa = Color.alpha(f) / 255f
            if (fa <= 0f) {
                out.setPixel(i % w, i / w, b)
                continue
            }
            val br = Color.red(b) / 255f; val bgc = Color.green(b) / 255f; val bb = Color.blue(b) / 255f
            val fr = Color.red(f) / 255f; val fgx = Color.green(f) / 255f; val fb = Color.blue(f) / 255f

            val (rr, gg, bbx) = when (mode) {
                Mode.NORMAL -> Triple(fr, fgx, fb)
                Mode.MULTIPLY -> Triple(br * fr, bgc * fgx, bb * fb)
                Mode.SCREEN -> Triple(1f - (1f - br) * (1f - fr), 1f - (1f - bgc) * (1f - fgx), 1f - (1f - bb) * (1f - fb))
                Mode.OVERLAY -> Triple(
                    if (br < .5f) 2f * br * fr else 1f - 2f * (1f - br) * (1f - fr),
                    if (bgc < .5f) 2f * bgc * fgx else 1f - 2f * (1f - bgc) * (1f - fgx),
                    if (bb < .5f) 2f * bb * fb else 1f - 2f * (1f - bb) * (1f - fb),
                )
            }

            // Alpha composite foreground over background
            val outR = ((rr * fa) + br * (1f - fa))
            val outG = ((gg * fa) + bgc * (1f - fa))
            val outB = ((bbx * fa) + bb * (1f - fa))
            val outA = min(1f, fa + Color.alpha(b) / 255f * (1f - fa))
            out.setPixel(i % w, i / w, Color.argb(
                (outA * 255f).toInt().coerceIn(0,255),
                (outR * 255f).toInt().coerceIn(0,255),
                (outG * 255f).toInt().coerceIn(0,255),
                (outB * 255f).toInt().coerceIn(0,255)
            ))
        }
        return out
    }
}