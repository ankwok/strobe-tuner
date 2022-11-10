package com.akwok.strobetuner

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.akwok.strobetuner.views.GaugeView
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
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
    fun symmetricAboutMiddle() {
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
    fun higherResolutionNearZero() {
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

    @Test
    fun angleInterpolationTakesShortestPath() {
        val testCases = listOf( // theta1, theta2, t, expected
            listOf(10f, 130f, 0.5f, 70f), // nominal positive direction
            listOf(200f, 300f, 0.3f, 230f), // nominal positive direction
            listOf(130f, 30f, 0.6f, 70f), // nominal negative direction
            listOf(350f, 10f, 0.4f, 358f), // crosses zero in positive direction
            listOf(350f, 10f, 0.5f, 0f), // crosses zero in positive direction
            listOf(350f, 10f, 0.6f, 2f), // crosses zero in positive direction
            listOf(10f, 350f, 0.4f, 2f), // crosses zero in negative direction
            listOf(10f, 350f, 0.5f, 0f), // crosses zero in negative direction
            listOf(10f, 350f, 0.6f, 358f), // crosses zero in negative direction
            listOf(200f, 100f, 0.6f, 140f), // shortest path is actually negative direction
            listOf(0f, 180f, 0.5f, 90f), // if 180 degrees, go in positive direction
            listOf(200f, 20f, 0.25f, 245f), // if 180 degrees, go in positive direction
            listOf(20f, 200f, 0.25f, 65f), // if 180 degrees, go in positive direction
        )

        testCases.forEach { case ->
            val theta1 = case[0]
            val theta2 = case[1]
            val t = case[2]
            val expected = case[3]

            val actual = GaugeView.interpAngle(theta1, theta2, t)
            Assert.assertEquals(expected, actual, 0.001f)
        }
    }

    @Test
    fun interpColorFromError() {
        val badColor = Color.BLACK
        val okColor = Color.WHITE
        val center = 100f
        val badPos = 150f
        val okPos = 110f

        val testCases = listOf( // needleCenter, expectedRGB
            listOf(0f, 0),
            listOf(50f, 0),
            listOf(150f, 0),
            listOf(90f, 0x00FFFFFF),
            listOf(100f, 0x00FFFFFF),
            listOf(110f, 0x00FFFFFF),
            listOf(51f, 0x00060606), // 1/40 * 256 = 6.4 ==> 0x06
            listOf(149f, 0x00060606),
        )

        testCases.forEach { case ->
            val needleCenter = case[0] as Float
            val expectedRgb = case[1] as Int
            val expectedRed = Color.red(expectedRgb)
            val expectedGreen = Color.green(expectedRgb)
            val expectedBlue = Color.blue(expectedRgb)

            val actualRgba =
                GaugeView.computeNeedleColor(needleCenter, center, badPos, okPos, badColor, okColor)

            Assert.assertEquals(expectedRed, Color.red(actualRgba))
            Assert.assertEquals(expectedGreen, Color.green(actualRgba))
            Assert.assertEquals(expectedBlue, Color.blue(actualRgba))
        }
    }
}