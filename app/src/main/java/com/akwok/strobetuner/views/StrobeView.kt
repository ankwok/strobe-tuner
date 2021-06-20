package com.akwok.strobetuner.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.VisibleForTesting
import java.time.Instant

class StrobeView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    constructor(context: Context) : this(context, null)

    var numBands: Int = 8
    var errorInCents: Float = 0f

    private val darkPaint = Paint().apply {
        isAntiAlias = true
        color = Color.argb(255, 32, 32, 32)
        style = Paint.Style.FILL_AND_STROKE
    }

    private val lightColor = Color.argb(255, 225, 225, 225)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var lastT: Long = Instant.now().toEpochMilli()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var lastOffset: Float = 0f

    private var refreshThread: Thread? = null
    private val refreshRateHz = 60

    var isRunning: Boolean = false
        set(newValue) {
            field = newValue

            if ((refreshThread == null || refreshThread?.isAlive == false) && newValue) {
                val refresher = Runnable {
                    val sleepDuration = (1000.0 / refreshRateHz).toLong()
                    while (isRunning) {
                        Thread.sleep(sleepDuration)
                        postInvalidate()
                    }
                }

                refreshThread = Thread(refresher)
                refreshThread!!.start()
            }
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            drawColor(lightColor)
            drawStrobeAndUpdateState(this)
        }
    }

    private fun drawStrobeAndUpdateState(canvas: Canvas) {
        val now = Instant.now().toEpochMilli()
        val dx = width.toFloat() / (2 * numBands)
        val heightFloat = height.toFloat()

        val offset = calcOffset(width, dx, errorInCents, now, lastT, lastOffset)

        (0 until numBands + 1)
            .forEach { i ->
                canvas.drawRect(
                    2 * i * dx + offset,
                    0f,
                    (2 * i + 1) * dx + offset,
                    heightFloat,
                    darkPaint
                )
            }

        lastT = now
        lastOffset = offset
    }

    companion object {
        const val scrollRate: Float = 0.05F // Percent of width per cent error per second

        fun calcOffset(
            widthPixels: Int,
            modulo: Float,
            errorInCents: Float,
            nowMillis: Long,
            lastMillis: Long,
            lastOffset: Float): Float {

            val rate = scrollRate * errorInCents * widthPixels
            val deltaSec = (nowMillis - lastMillis).toFloat() / 1000f
            val newOffset = lastOffset + rate * deltaSec

            return when {
                newOffset > modulo -> newOffset - 2*modulo
                newOffset < -modulo -> newOffset + 2*modulo
                else -> newOffset
            }
        }
    }
}