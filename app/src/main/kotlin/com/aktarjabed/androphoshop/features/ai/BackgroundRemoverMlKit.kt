package com.aktarjabed.androphoshop.features.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenter
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.hypot

class BackgroundRemoverMlKit {
  private val segmenter = SelfieSegmenter.getClient(
    SelfieSegmenterOptions.Builder()
      .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
      .enableRawSizeMask()
      .build()
  )
  suspend fun removeBackground(src: Bitmap, focus: PointF? = null): Bitmap = suspendCancellableCoroutine { cont ->
    val image = InputImage.fromBitmap(src, 0)
    segmenter.process(image)
      .addOnSuccessListener { mask ->
        try {
          val mw = mask.width; val mh = mask.height
          val buf = mask.buffer; buf.rewind()
          val alpha = Bitmap.createBitmap(mw, mh, Bitmap.Config.ALPHA_8)
          val arr = ByteArray(mw*mh)

          val fx = focus?.x?.times(mw / src.width.toFloat()) ?: mw/2f
          val fy = focus?.y?.times(mh / src.height.toFloat()) ?: mh/2f
          val maxDist = hypot(mw.toFloat(), mh.toFloat())

          var i = 0
          while (buf.hasRemaining()) {
            var v = buf.get() // 0..1
            if (focus != null) {
              val x = (i % mw).toFloat()
              val y = (i / mw).toFloat()
              val fall = (1f - (hypot(x - fx, y - fy) / maxDist)).coerceIn(0f, 1f)
              v *= 0.5f + 0.5f * fall
            }
            arr[i++] = (v * 255f).toInt().toByte()
          }
          alpha.copyPixelsFromBuffer(ByteBuffer.wrap(arr))
          val soft = MaskRefiner.featherEdges(alpha, 6f)
          val out = MaskRefiner.applyAlphaMask(src, soft)
          cont.resume(out)
        } catch (e: Exception) { cont.resumeWithException(e) }
      }
      .addOnFailureListener { cont.resumeWithException(it) }
  }
}