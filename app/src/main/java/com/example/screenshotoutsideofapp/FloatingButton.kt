package com.example.screenshotoutsideofapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import java.util.*

@SuppressLint("ClickableViewAccessibility")
class FloatingButton(
    context: Context,
    private val onClick: (() -> Unit)?
) {

    var visible: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    windowManager.addView(view, params)
                } else {
                    windowManager.removeView(view)
                }
            }
        }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val view = ImageView(context).apply {
        setImageResource(R.drawable.ic_baseline_photo_camera_24)
        setBackgroundResource(R.drawable.shape_circle_filled)
        val padding = (16 * resources.displayMetrics.density).toInt()
        setPadding(padding, padding, padding, padding)
    }
    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 100
        y = 150
    }
    private var initial: Position? = null

    init {
        view.setOnTouchListener(object : View.OnTouchListener {
            private val MAX_CLICK_DURATION = 200
            private var startClickTime: Long = 0

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initial = params.position - event.position
                        startClickTime = Calendar.getInstance().timeInMillis
                    }
                    MotionEvent.ACTION_MOVE -> {
                        initial?.let {
                            params.position = it + event.position
                            windowManager.updateViewLayout(view, params)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        initial = null
                        val clickDuration = Calendar.getInstance().timeInMillis - startClickTime
                        if (clickDuration < MAX_CLICK_DURATION) {
                            onClick?.invoke()
                        }
                    }
                }
                return true
            }
        })
    }

    private val MotionEvent.position: Position
        get() = Position(rawX, rawY)

    private var WindowManager.LayoutParams.position: Position
        get() = Position(x.toFloat(), y.toFloat())
        set(value) {
            x = value.x
            y = value.y
        }

    private data class Position(val fx: Float, val fy: Float) {

        val x: Int
            get() = fx.toInt()

        val y: Int
            get() = fy.toInt()

        operator fun plus(p: Position) = Position(fx + p.fx, fy + p.fy)
        operator fun minus(p: Position) = Position(fx - p.fx, fy - p.fy)
    }
}