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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class RotorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var rotationAngle = 0f
    private var animator: ValueAnimator? = null
    private var speedFactor = 6.5f
    private var amplitudeFactor = 0.9f
    private var phaseOffsetDegrees = 0f
    private var isRunning = true

    private val phaseColors = listOf(
        Color.parseColor("#F87171"),
        Color.parseColor("#4ADE80"),
        Color.parseColor("#60A5FA")
    )

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7DD3FC")
        style = Paint.Style.FILL
        alpha = 120
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    fun setSpeed(speed: Float) {
        speedFactor = speed.coerceIn(0.5f, 12f)
        restartAnimation()
    }

    fun setAmplitude(amplitude: Float) {
        amplitudeFactor = amplitude.coerceIn(0.4f, 1.4f)
        invalidate()
    }

    fun setPhaseOffset(offset: Float) {
        phaseOffsetDegrees = offset.coerceIn(0f, 360f)
        invalidate()
    }

    fun setRunning(running: Boolean) {
        isRunning = running
        if (running) restartAnimation() else animator?.cancel()
    }

    fun resetState() {
        speedFactor = 6.5f
        amplitudeFactor = 0.9f
        phaseOffsetDegrees = 0f
        rotationAngle = 0f
        invalidate()
        restartAnimation()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        restartAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator = null
    }

    private fun restartAnimation() {
        animator?.cancel()
        if (!isRunning) return

        val duration = (2200f / speedFactor).toLong().coerceAtLeast(250L)

        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            this.duration = duration
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
            color = Color.parseColor("#0B1220")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        for (i in 1..5) {
            val r = radius * (i / 5f)
            canvas.drawCircle(cx, cy, r, ringPaint)
        }

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            alpha = 35
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        for (i in 0..10) {
            val x = cx - radius + (i * radius * 2f / 10f)
            canvas.drawLine(x, cy - radius, x, cy + radius, gridPaint)
        }

        for (i in 0..10) {
            val y = cy - radius + (i * radius * 2f / 10f)
            canvas.drawLine(cx - radius, y, cx + radius, y, gridPaint)
        }
    }

    private fun drawPhaseRotor(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val phaseAngles = listOf(0f, 120f, 240f)
        val drawRadius = radius * amplitudeFactor

        for ((index, angleOffset) in phaseAngles.withIndex()) {
            val actualAngle = rotationAngle + angleOffset + phaseOffsetDegrees
            val angleRad = Math.toRadians(actualAngle.toDouble())
            val x = cx + cos(angleRad) * drawRadius
            val y = cy + sin(angleRad) * drawRadius

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = phaseColors[index]
                style = Paint.Style.STROKE
                strokeWidth = 8f
            }

            canvas.drawLine(cx, cy, x.toFloat(), y.toFloat(), linePaint)

            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = phaseColors[index]
                style = Paint.Style.FILL
            }
            canvas.drawCircle(x.toFloat(), y.toFloat(), 14f, dotPaint)

            val glowRadius = 18f + (index + 1) * 8f
            canvas.drawCircle(x.toFloat(), y.toFloat(), glowRadius, Paint().apply {
                color = phaseColors[index]
                alpha = 70
                style = Paint.Style.FILL
            })
        }

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            alpha = 180
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(cx, cy, drawRadius * 0.72f, ringPaint)

        val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            alpha = 110
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val arcRect = RectF(
            cx - drawRadius * 0.72f,
            cy - drawRadius * 0.72f,
            cx + drawRadius * 0.72f,
            cy + drawRadius * 0.72f
        )
        canvas.drawArc(arcRect, rotationAngle, 125f, false, arcPaint)

        val path = Path()
        path.moveTo(cx, cy)

        val p1a = Math.toRadians((rotationAngle + phaseOffsetDegrees).toDouble())
        val p1x = cx + cos(p1a) * (drawRadius * 0.85f)
        val p1y = cy + sin(p1a) * (drawRadius * 0.85f)
        path.lineTo(p1x.toFloat(), p1y.toFloat())

        val p2a = Math.toRadians((rotationAngle + 120f + phaseOffsetDegrees).toDouble())
        val p2x = cx + cos(p2a) * (drawRadius * 0.85f)
        val p2y = cy + sin(p2a) * (drawRadius * 0.85f)
        path.lineTo(p2x.toFloat(), p2y.toFloat())

        val p3a = Math.toRadians((rotationAngle + 240f + phaseOffsetDegrees).toDouble())
        val p3x = cx + cos(p3a) * (drawRadius * 0.85f)
        val p3y = cy + sin(p3a) * (drawRadius * 0.85f)
        path.lineTo(p3x.toFloat(), p3y.toFloat())
        path.close()

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(70, 148, 163, 184)
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, fillPaint)
    }

    private fun drawCenterCore(canvas: Canvas, cx: Float, cy: Float) {
        canvas.drawCircle(cx, cy, 60f, glowPaint)
        canvas.drawCircle(cx, cy, 32f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        })

        canvas.drawCircle(cx, cy, 44f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0EA5E9")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        })

        canvas.drawText("3φ", cx, cy + 10f, labelPaint)
    }
}

