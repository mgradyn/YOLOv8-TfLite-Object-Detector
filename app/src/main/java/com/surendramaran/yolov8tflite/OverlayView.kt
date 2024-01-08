package com.surendramaran.yolov8tflite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results = listOf<BoundingBox>()
    private val textBackgroundPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = 50f
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        textSize = 50f
    }
    private val bounds = Rect()

    fun clear() {
        results = listOf()
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        results.forEach { drawBoundingBox(canvas, it) }
    }

    private val availableColors = listOf(
        "rgb(255, 100, 100)", // Higher Contrast Red
        "rgb(100, 255, 100)", // Higher Contrast Green
        "rgb(100, 100, 255)", // Higher Contrast Blue
        "rgb(255, 255, 100)", // Higher Contrast Yellow
        "rgb(255, 100, 255)"  // Higher Contrast Purple
    )

    private val classColorMap = mutableMapOf<String, Paint>()

    private fun getColorPaintForClass(className: String): Paint {
        return classColorMap.getOrPut(className) {
            val colorString = availableColors.random()
            val rgb = colorString.removePrefix("rgb(").removeSuffix(")").split(",").map { it.trim().toInt() }
            Paint().apply {
                color = Color.rgb(rgb[0], rgb[1], rgb[2])
                strokeWidth = 8f
                style = Paint.Style.STROKE
            }
        }
    }

    private fun drawBoundingBox(canvas: Canvas, box: BoundingBox) {
        val left = box.x1 * width
        val top = box.y1 * height
        val right = box.x2 * width
        val bottom = box.y2 * height

        val boxPaint = getColorPaintForClass(box.clsName)

        canvas.drawRect(left, top, right, bottom, boxPaint)

        val drawableText = "${box.clsName} %.2f".format(box.cnf)
        textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
        canvas.drawRect(
            left,
            top,
            left + bounds.width() + BOUNDING_RECT_TEXT_PADDING * 2,
            top + bounds.height() + BOUNDING_RECT_TEXT_PADDING * 2,
            textBackgroundPaint
        )
        canvas.drawText(drawableText, left + BOUNDING_RECT_TEXT_PADDING, top + bounds.height() + BOUNDING_RECT_TEXT_PADDING, textPaint)
    }

    fun setResults(boundingBoxes: List<BoundingBox>) {
        results = boundingBoxes
        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}