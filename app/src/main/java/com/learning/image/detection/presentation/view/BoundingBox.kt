package com.learning.image.detection.presentation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

class BoundingBox(context: Context?, var rect: Rect) : View(context) {

    lateinit var paint: Paint

    init {
        init()
    }

    private fun init() {
        paint = Paint()
        paint.color = Color.GREEN
        paint.strokeWidth = 8f
        paint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat(),
            paint
        )
    }
}