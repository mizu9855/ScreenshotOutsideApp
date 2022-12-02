package com.example.screenshotoutsideofapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection

class Capture(private val context: Context) : ImageReader.OnImageAvailableListener {

    private var display: VirtualDisplay? = null
    private var onCaptureListener: ((Bitmap) -> Unit)? = null

    override fun onImageAvailable(reader: ImageReader) {
        if (display != null) {
            onCaptureListener?.invoke(captureImage(reader))
        }
    }

    fun run(mediaProjection: MediaProjection, onCaptureListener: (Bitmap) -> Unit) {
        this.onCaptureListener = onCaptureListener
        if (display == null) {
            display = createDisplay(mediaProjection)
        }
    }

    fun stop() {
        display?.release()
        display = null
        onCaptureListener = null
    }

    private fun createDisplay(mediaProjection: MediaProjection): VirtualDisplay {
        context.resources.displayMetrics.run {
            val maxImages = 2
            // PixelFormat.RGBA_8888がエラーになる場合があるが、ビルドできる
            val reader = ImageReader.newInstance(
                widthPixels, heightPixels, PixelFormat.RGBA_8888, maxImages
            )
            reader.setOnImageAvailableListener(this@Capture, null)
            return mediaProjection.createVirtualDisplay(
                "Capture Display", widthPixels, heightPixels, densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.surface, null, null
            )
        }
    }

    private fun captureImage(reader: ImageReader): Bitmap {
        val image = reader.acquireLatestImage()
        context.resources.displayMetrics.let { displayMatrics ->
            image.planes[0].run {
                val bitmap = Bitmap.createBitmap(
                    rowStride / pixelStride, displayMatrics.heightPixels, Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                image.close()
                return bitmap
            }
        }
    }
}