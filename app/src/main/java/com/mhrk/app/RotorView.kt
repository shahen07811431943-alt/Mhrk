package com.mhrk.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min

class RotorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var rotationAngle = 0f
    private var animator: ValueAnimator? = null

    private val outerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#334155")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F8FAFC")
        style = Paint.Style.FILL
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7DD3FC")
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#38BDF8")
        style = Paint.Style.FILL
        alpha = 90
    }

    private val phaseColors = listOf(
        Color.parseColor("#F87171"),
        Color.parseColor("#4ADE80"),
        Color.parseColor("#60A5FA")
    )

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator = null
    }

    private fun startAnimation() {
        if (animator != null) return

        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 2200
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener { valueAnimator ->
                rotationAngle = valueAnimator.animatedValue as Float
                invalidate()
            }
        }
        animator?.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) * 0.32f

        drawBackground(canvas, cx, cy, radius)
        drawPhaseRotor(canvas, cx, cy, radius)
        drawCenterCore(canvas, cx, cy)
    }

    private fun drawBackground(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        for (i in 1..4) {
            val r = radius * (i / 4f)
            canvas.drawCircle(cx, cy, r, ringPaint)
        }

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1D4ED8")
            alpha = 40
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        for (i in 0..8) {
            val x = cx - radius + (i * radius * 2f / 8f)
            canvas.drawLine(x, cy - radius, x, cy + radius, gridPaint)
        }

        for (i in 0..8) {
            val y = cy - radius + (i * radius * 2f / 8f)
            canvas.drawLine(cx - radius, y, cx + radius, y, gridPaint)
        }
    }

    private fun drawPhaseRotor(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val phaseAngles = listOf(0f, 120f, 240f)

        for ((index, angleOffset) in phaseAngles.withIndex()) {
            val angleRad = Math.toRadians((rotationAngle + angleOffset).toDouble())
            val x = cx + cos(angleRad) * radius
            val y = cy + sin(angleRad) * radius

            val color = phaseColors[index]

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                style = Paint.Style.STROKE
                strokeWidth = 8f
            }

            canvas.drawLine(cx, cy, x.toFloat(), y.toFloat(), linePaint)

            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                style = Paint.Style.FILL
            }
            canvas.drawCircle(x.toFloat(), y.toFloat(), 14f, dotPaint)

            val perspective = 70f
            val px = x.toFloat() + (cos(Math.toRadians((angleOffset + 30).toDouble())) * perspective).toFloat()
            val py = y.toFloat() + (sin(Math.toRadians((angleOffset + 30).toDouble())) * perspective).toFloat()
            canvas.drawCircle(px, py, 10f, Paint().apply {
                color = Color.argb(120, 255, 255, 255)
                style = Paint.Style.FILL
            })
        }

        val rotorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            alpha = 180
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(cx, cy, radius * 0.72f, rotorPaint)

        val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            alpha = 90
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val arcRect = RectF(cx - radius * 0.72f, cy - radius * 0.72f, cx + radius * 0.72f, cy + radius * 0.72f)
        canvas.drawArc(arcRect, rotationAngle, 120f, false, arcPaint)

        val path = Path()
        path.moveTo(cx, cy)
        val p1a = Math.toRadians((rotationAngle + 0).toDouble())
        val p1x = cx + cos(p1a) * (radius * 0.8f)
        val p1y = cy + sin(p1a) * (radius * 0.8f)
        path.lineTo(p1x.toFloat(), p1y.toFloat())

        val p2a = Math.toRadians((rotationAngle + 120.0).toDouble())
        val p2x = cx + cos(p2a) * (radius * 0.8f)
        val p2y = cy + sin(p2a) * (radius * 0.8f)
        path.lineTo(p2x.toFloat(), p2y.toFloat())

        val p3a = Math.toRadians((rotationAngle + 240.0).toDouble())
        val p3x = cx + cos(p3a) * (radius * 0.8f)
        val p3y = cy + sin(p3a) * (radius * 0.8f)
        path.lineTo(p3x.toFloat(), p3y.toFloat())
        path.close()

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(50, 148, 163, 184)
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, fillPaint)
    }

    private fun drawCenterCore(canvas: Canvas, cx: Float, cy: Float) {
        val outerGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7DD3FC")
            style = Paint.Style.FILL
            alpha = 120
        }
        canvas.drawCircle(cx, cy, 55f, outerGlow)

        canvas.drawCircle(cx, cy, 32f, centerPaint)

        val centerRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0EA5E9")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(cx, cy, 42f, centerRing)

        canvas.drawText("3φ", cx, cy + 12f, labelPaint)
    }
}
