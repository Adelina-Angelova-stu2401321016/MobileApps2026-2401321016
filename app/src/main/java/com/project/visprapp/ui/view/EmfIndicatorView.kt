package com.project.visprapp.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt

class EmfIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x22000000
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF444444.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val clipPath = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = width.coerceAtMost(height) / 2f
        val cx = width / 2f
        val cy = height / 2f

        canvas.drawCircle(cx, cy, radius, backgroundPaint)
        if (progress > 0f) {
            canvas.save()
            clipPath.reset()
            clipPath.addCircle(cx, cy, radius, Path.Direction.CW)
            canvas.clipPath(clipPath)

            val fillHeight = height * progress
            val topY = height - fillHeight

            fillPaint.color = interpolateColor(0xFF00FF00.toInt(), 0xFFFF0000.toInt(), progress)

            canvas.drawRect(0f, topY, width, height, fillPaint)
            canvas.restore()
        }

        canvas.drawCircle(cx, cy, radius, borderPaint)
    }

    private fun interpolateColor(colorStart: Int, colorEnd: Int, fraction: Float): Int {
        val rStart = (colorStart shr 16) and 0xFF
        val gStart = (colorStart shr 8) and 0xFF
        val bStart = colorStart and 0xFF

        val rEnd = (colorEnd shr 16) and 0xFF
        val gEnd = (colorEnd shr 8) and 0xFF
        val bEnd = colorEnd and 0xFF

        val r = (rStart + fraction * (rEnd - rStart)).roundToInt()
        val g = (gStart + fraction * (gEnd - gStart)).roundToInt()
        val b = (bStart + fraction * (bEnd - bStart)).roundToInt()

        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

}