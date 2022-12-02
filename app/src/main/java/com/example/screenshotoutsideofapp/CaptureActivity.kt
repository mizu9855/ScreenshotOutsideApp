package com.example.screenshotoutsideofapp

import android.app.Activity
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CaptureActivity : Activity() {

    companion object {
        private const val REQUEST_CAPTURE = 1

        var mediaProjection: MediaProjection? = null
        private lateinit var mediaProjectionManager: MediaProjectionManager

        private var mResultCode: Int? = null
        private var mData: Intent? = null

        fun setMediaProjection() {
            mediaProjection = mediaProjectionManager.getMediaProjection(
                mResultCode ?: return,
                mData ?: return
            )
        }
    }

    private val capture: Capture = Capture(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaProjectionManager = getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CAPTURE) {
            mResultCode = resultCode
            mData = data

            if (resultCode == RESULT_OK) {
                val floatingServiceIntent = Intent(this, FloatingViewService::class.java)
                    .setAction(FloatingViewService.ACTION_START)
                startService(floatingServiceIntent)
                FloatingViewService.setOnClickListener {
                    capture()
                }

                val captureServiceIntent = Intent(this, CaptureService::class.java)
                    .setAction(CaptureService.ACTION_START)
                startService(captureServiceIntent)

            } else {
                mediaProjection = null
            }
        }
        finish()
    }

    private fun capture() {
        mediaProjection?.let { mediaProjection ->
            capture.run(mediaProjection) { bitmap ->
                saveBitmap(bitmap)
                capture.stop()
            }
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        if (!isExternalStorageWritable()) {
            Toast.makeText(this, "保存に失敗しました", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("yyyy,MM,dd,HH:mm:ss", Locale.JAPAN)
        val currentTime = dateFormat.format(Calendar.getInstance().time)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$currentTime.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = contentResolver
            val collection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val item = resolver.insert(collection, values)

            val outputStream = item?.let { resolver.openOutputStream(it) }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            values.apply {
                clear()
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            if (item != null) {
                resolver.update(item, values, null, null)
            }
            outputStream?.close()
        } else {
            val filePath =
                File(Environment.getExternalStorageDirectory().path + "/" + Environment.DIRECTORY_DCIM + "/" + currentTime + ".png")
            val outputStream = FileOutputStream(filePath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        }

        Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show()
    }

    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }
}