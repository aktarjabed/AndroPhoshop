package com.aktarjabed.androphoshop.features.ai

import android.graphics.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Adds a controllable rim light around the subject edges (from alpha).
 * Works fully offline; fast on-device.
 */
class RelightEngine {

    data class Params(
        val intensity: Float = 0.35f,   // 0..1
        val radiusPx: Float = 16f,      // rim width
        val color: Int = Color.WHITE,   // rim color
        val directionDeg: Float = 30f   // where the light comes from
    )

    /**
     * @param subject ARGB cutout (subject with transparent background)
     */
    fun addRimLight(subject: Bitmap, params: Params): Bitmap {
        val w = subject.width
        val h = subject.height

        // 1) Build edge map from alpha gradient
        val alphaOnly = subject.extractAlpha()
        // Simple gradient highlights: dilate alpha and subtract base -> edge band
        val dilated = MaskRefiner.featherEdges(alphaOnly, params.radiusPx)
        val edge = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        val cEdge = Canvas(edge)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        }
        cEdge.drawBitmap(dilated, 0f, 0f, p)
        p.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        cEdge.drawBitmap(alphaOnly, 0f, 0f, p)
        p.xfermode = null

        // 2) Tint rim to chosen color + intensity
        val rim = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val cRim = Canvas(rim)
        val rp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = params.color
            alpha = (params.intensity * 255).toInt().coerceIn(0, 255)
        }
        cRim.drawBitmap(edge, 0f, 0f, rp)

        // 3) Directional bias: shift rim slightly opposite to light direction
        val shift = params.radiusPx / 3f
        val rad = Math.toRadians(params.directionDeg.toDouble())
        val dx = (-shift * kotlin.math.cos(rad)).toFloat()
        val dy = (-shift * kotlin.math.sin(rad)).toFloat()
        val shifted = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val cShift = Canvas(shifted)
        cShift.drawBitmap(rim, dx, dy, null)

        // 4) Composite rim over subject
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val cOut = Canvas(out)
        cOut.drawBitmap(subject, 0f, 0f, null)
        cOut.drawBitmap(shifted, 0f, 0f, null)
        return out
    }
}