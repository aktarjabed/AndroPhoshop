package com.aktarjabed.androphoshop.features.ai

import android.graphics.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Real, on-device background removal using ML Kit Selfie Segmentation.
 * Produces an RGBA bitmap where background pixels are transparent.
 */
class BackgroundRemoverMlKit {

    private val options = SelfieSegmenterOptions.Builder()
        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
        .enableRawSizeMask() // higher quality mask
        .build()

    private val segmenter by lazy { Segmentation.getClient(options) }

    suspend fun removeBackground(src: Bitmap, focus: PointF? = null): Bitmap = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(src, 0)
        segmenter.process(image)
            .addOnSuccessListener { mask ->
                try {
                    // ML Kit returns a FloatArray mask (values 0..1) with size of the analysis image.
                    val maskBuffer = mask.buffer  // FloatBuffer
                    val mw = mask.width
                    val mh = mask.height

                    // Convert FloatBuffer -> Int alpha map and scale to src size if needed
                    val alphaBitmap = Bitmap.createBitmap(mw, mh, Bitmap.Config.ALPHA_8)
                    val alphaArray = ByteArray(mw * mh)
                    maskBuffer.rewind()
                    var i = 0
                    while (maskBuffer.hasRemaining()) {
                        val confidence = maskBuffer.get() // foreground probability [0..1]
                        val a = (confidence * 255f).toInt().coerceIn(0, 255)
                        alphaArray[i++] = a.toByte()
                    }
                    alphaBitmap.copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(alphaArray))

                    // Scale alpha to source size when ML mask size != source size
                    val scaledAlpha = if (mw != src.width || mh != src.height) {
                        Bitmap.createScaledBitmap(alphaBitmap, src.width, src.height, true)
                    } else alphaBitmap

                    val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(out)

                    // Draw the source with alpha mask (DST_IN keeps only foreground)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        // draw source first
                    }
                    canvas.drawBitmap(src, 0f, 0f, paint)

                    // Apply alpha mask using DST_IN so destination keeps only masked (foreground) pixels
                    val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
                    }
                    canvas.drawBitmap(scaledAlpha, 0f, 0f, maskPaint)
                    maskPaint.xfermode = null

                    cont.resume(out)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }

        cont.invokeOnCancellation {
            // nothing to cancel explicitly in ML Kit single-image mode
        }
    }
}