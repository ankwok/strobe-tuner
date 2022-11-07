package com.akwok.strobetuner.views

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.akwok.strobetuner.R
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt

class GaugeView(context: Context, attrs: AttributeSet?) : TunerView(context, attrs) {
    constructor(context: Context) : this(context, null)

    private var needleCenter: Float = -1f
    private var needleVelocity: Float = 0f

    override var octave: Int = 0

    override var errorInCents: Float? = null
        set(value) {
            if ((value != null) && (value != 0f)) {
                field = value
            }
        }

    private val widthPadding: Float
        get() = if (width > 0) paddingPct * width else 0f

    private val heightPadding: Float
        get() = if (height > 0) paddingPct * height else 0f

    private val needlePaint = Paint().apply {
        isAntiAlias = true
        color = resources.getColor(R.color.needle_color, resources.newTheme())
        style = Paint.Style.FILL_AND_STROKE
    }

    private val tickColor = Paint().apply {
        isAntiAlias = true
        color = resources.getColor(R.color.tick_color, resources.newTheme())
        style = Paint.Style.FILL_AND_STROKE
    }

    private val animator = TimeAnimator()

    init {
        animator.setTimeListener { _, _, deltaTime ->
            val err = errorInCents
            if (err != null && width > 0) {
                val nextPos = computeNeedleCenter(err, width, widthPadding)
                val delta = nextPos - needleCenter

                needleVelocity += 0.001f * deltaTime * (kp * delta - damping * needleVelocity)
                needleCenter += 0.001f * deltaTime * needleVelocity

                postInvalidate()
            }
        }
    }

    override fun postInvalidate() {
        if (!needleCenter.isFinite()) {
            needleCenter = width.toFloat() / 2
        }

        super.postInvalidate()
    }

    override fun start() {
        if (animator.isPaused || !animator.isRunning || !animator.isStarted) {
            animator.start()
        }
    }

    override fun pause() {
        animator.pause()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // The view may not be completely initialized so only draw if there's a positive width
        if (width > 0) {
            canvas.apply {
                drawAndUpdateState(this)
            }
        }
    }

    private fun drawAndUpdateState(canvas: Canvas) {
        drawTicks(canvas)
        drawNeedle(canvas)
    }

    private fun drawTicks(canvas: Canvas) {
        val majorTickTop = height.toFloat() / 2 - height.toFloat() / 8
        val majorTickBottom = height.toFloat() / 2 + height.toFloat() / 8
        val majorTickHalfWidth = majorTicksWidthPct * width / 2
        val majorTickCenters = majorTicks.map { x -> computeNeedleCenter(x, width, widthPadding) }
        majorTickCenters.forEach { center ->
            canvas.drawRect(
                center - majorTickHalfWidth,
                majorTickTop,
                center + majorTickHalfWidth,
                majorTickBottom,
                tickColor
            )
        }

        val minorTickTop = height.toFloat() / 2 - height.toFloat() / 16
        val minorTickBottom = height.toFloat() / 2 + height.toFloat() / 16
        val minorTickHalfWidth = minorTicksWidthPct * width / 2
        val minorTickCenters = minorTicks.map { x -> computeNeedleCenter(x, width, widthPadding) }
        minorTickCenters.forEach { center ->
            canvas.drawRect(
                center - minorTickHalfWidth,
                minorTickTop,
                center + minorTickHalfWidth,
                minorTickBottom,
                tickColor
            )
        }
    }

    private fun drawNeedle(canvas: Canvas) {
        if (needleCenter <= 0) { return }

        val needleWidth = width * needleWidthPct
        val halfWidth = needleWidth / 2
        canvas.drawRect(
            needleCenter - halfWidth,
            2 * heightPadding,
            needleCenter + halfWidth,
            height - heightPadding,
            needlePaint
        )
    }

    companion object {
        private const val paddingPct = 0.05f
        private const val needleWidthPct = 0.01f // percent of width
        private const val exponent = 1 / 1.75
        private const val kp = 20f
        private val damping = sqrt(4 * kp) // critically damped

        private val majorTicks = (-10 .. 10).map { i -> 5f * i }
        private const val majorTicksWidthPct = 0.01f
        private val minorTicks = (-50 .. 50)
            .map { x -> x.toFloat() }
            .filterNot { x -> majorTicks.contains(x) }
        private const val minorTicksWidthPct = 0.004f

        fun computeNeedleCenter(errorInCents: Float, width: Int, padding: Float): Float {
            val mid = width / 2

            val halfWidth = width.toDouble() / 2 - padding
            val scaleFactor = halfWidth.pow(1 / exponent) / 50
            val offset = (abs(errorInCents) * scaleFactor).pow(exponent)

            return mid + errorInCents.sign * offset.toFloat()
        }
    }
}