package com.akwok.strobetuner.views

import org.junit.Assert
import org.junit.Test

class StrobeViewUnitTest {

    @Test
    fun calcOffset() {
        val cases = listOf<Triple<String, List<Any>, Float>>(
            Triple("No error, no offset", listOf(1000, 100f, 0f, 10L, 0L, 0f), 0f),
            Triple("Positive error, move to the right", listOf(1000, 100f, 0.5f, 1000L, 0L, 0f), 25f),
            Triple("Negative error, move to the left", listOf(1000, 100f, -0.5f, 1000L, 0L, 0f), -25f),
            Triple("Wrap around right", listOf(1000, 100f, 0.5f, 1000L, 0L, 90f), -85f),
            Triple("Wrap around left", listOf(1000, 100f, -0.5f, 1000L, 0L, -95f), 80f),
        )

        cases.forEach { trip -> calcOffset(trip.first, trip.second, trip.third) }
    }

    private fun calcOffset(description: String, args: List<Any>, expected: Float) {
        val widthPixels = args[0] as Int
        val modulo = args[1] as Float
        val errorInCents  = args[2] as Float
        val nowMillis = args[3] as Long
        val lastMillis = args[4] as Long
        val lastOffset = args[5] as Float
        val offset = StrobeView.calcOffset(widthPixels, modulo, errorInCents, nowMillis, lastMillis, lastOffset)

        Assert.assertEquals(description, expected, offset, 0.1f)
    }
}