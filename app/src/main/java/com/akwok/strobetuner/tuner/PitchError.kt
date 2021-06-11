package com.akwok.strobetuner.tuner

data class PitchError(val expected: Pitch, val actualFreq: Double, val ci: Interval) {
    private val check = require(actualFreq in ci.low..ci.high)

    val deltaFreq: Double = actualFreq - expected.freq
}

data class Interval(val low: Double, val high: Double)
