package com.akwok.strobetuner.tuner

import kotlin.math.log

data class PitchError(val expected: Pitch, val actualPeriod: Double, val variance: Double) {
    val actualFreq: Double = 1.0 / actualPeriod

    val errorInCents: Double by lazy {
        // f * r**x = y
        // log(f) + x log(r) = log(y)
        // x = (log(y) - log(f)) / log(r)
        log(actualFreq, PitchHelper.centRatio) - log(expected.freq, PitchHelper.centRatio)
    }
}
