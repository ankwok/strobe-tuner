package com.akwok.strobetuner.views

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.akwok.strobetuner.R
import java.lang.Float.max
import java.lang.Float.min

class StrobeView(context: Context, attrs: AttributeSet?) : TunerView(context, attrs) {
    constructor(context: Context) : this(context, null)

    override var octave: Int = 0
    override var errorInCents: Float = 0f

    private fun numBands(): Int = 2 * (octave + 1)

    private val darkPaint = Paint().apply {
        isAntiAlias = true
        color = resources.getColor(R.color.dark_strobe, resources.newTheme())
        style = Paint.Style.FILL_AND_STROKE
    }

    private val lightColor = Paint().apply {
        isAntiAlias = true
        color = resources.getColor(R.color.light_strobe, resources.newTheme())
        style = Paint.Style.FILL_AND_STROKE
    }

    private var lastOffset: Float = 0f
    private var deltaT: Long = 0L
    private var animatorClock: Long = 0

    private val animator = TimeAnimator()

    init {
        animator.setTimeListener { _, _, deltaTime ->
            deltaT = deltaTime
            postInvalidate()
        }
    }

    override fun start() {
        if (animator.isPaused || !animator.isRunning || !animator.isStarted) {
            animator.start()
            animator.currentPlayTime = animatorClock
        }
    }

    override fun pause() {
        animatorClock = animator.currentPlayTime
        animator.pause()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            drawStrobeAndUpdateState(this)
        }
    }

    private fun drawStrobeAndUpdateState(canvas: Canvas) {
        val bands = numBands()
        val dx = (width - 2 * padding) / (2 * bands)

        canvas.drawRect(padding, padding, width - padding, height - padding, lightColor)

        val offset = calcOffset(width - 2 * padding.toInt(), dx, errorInCents, deltaT, lastOffset)

        (0 until bands + 1)
            .forEach { i ->
                val left = max(padding, padding + 2 * i * dx + offset)
                val right = min(width - padding, padding + (2 * i + 1) * dx + offset)
                if (left < right) {
                    canvas.drawRect(left, padding, right, height - padding, darkPaint)
                }
            }

        lastOffset = offset
    }

    companion object {
        private const val scrollRate: Float = 0.05F // Percent of width per cent error per second
        const val padding: Float = 10f

        fun calcOffset(
            widthPixels: Int,
            modulo: Float,
            errorInCents: Float,
            deltaMillis: Long,
            lastOffset: Float
        ): Float {
            val rate = scrollRate * errorInCents * widthPixels
            val deltaSec = deltaMillis.toFloat() / 1000f
            val newOffset = lastOffset + rate * deltaSec
            return when {
                        newOffset > modulo -> newOffset - 2 * modulo
                        newOffset < -modulo -> newOffset + 2 * modulo
                        else -> newOffset
                    }
        }
    }
}