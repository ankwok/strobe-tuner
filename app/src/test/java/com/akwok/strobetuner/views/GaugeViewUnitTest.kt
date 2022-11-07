package com.akwok.strobetuner.views

import org.junit.Assert
import org.junit.Test

class GaugeViewUnitTest {

    @Test
    fun calcNeedlePositionExtremes() {
        val width = 100
        val cases = listOf(
            listOf(0f, 0f, 50f),
            listOf(50f, 0f, 100f),
            listOf(-50f, 0f, 0f),
            listOf(0f, 10f, 50f),
            listOf(50f, 10f, 90f),
            listOf(-50f, 10f, 10f),
        )

        cases.forEach { case ->
            val errCents = case[0]
            val padding = case[1]
            val expected = case[2]

            val pos = GaugeView.computeNeedleCenter(errCents, width, padding)

            Assert.assertEquals(expected, pos, 0.001f)
        }
    }

    @Test
    fun `symmetric about middle`() {
        val width = 100
        val padding = 0f

        val mid = width.toFloat() / 2
        (0 .. 50).forEach { errCents ->
            val posCenter = GaugeView.computeNeedleCenter(errCents.toFloat(), width, padding)
            val negCenter = GaugeView.computeNeedleCenter(-errCents.toFloat(), width, padding)

            val delta1 = posCenter - mid
            val delta2 = mid - negCenter
            Assert.assertEquals(delta1, delta2, 0.001f)
        }
    }

    @Test
    fun `higher resolution near zero`() {
        val width = 100
        val padding = 0f

        (0 .. 49)
            .forEach { errCents ->
                val center0 = GaugeView.computeNeedleCenter(errCents.toFloat(), width, padding)
                val center1 = GaugeView.computeNeedleCenter(errCents.toFloat() + 1, width, padding)
                val center2 = GaugeView.computeNeedleCenter(errCents.toFloat() + 2, width, padding)

                val delta1 = center1 - center0
                val delta2 = center2 - center1
                Assert.assertTrue(delta2 < delta1)
            }
    }
}