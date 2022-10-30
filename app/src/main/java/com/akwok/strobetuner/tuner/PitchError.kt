package com.akwok.strobetuner.tuner

import kotlin.math.log

data class PitchError(val expected: Pitch, val actualPeriod: Double, val std: Double) {
    val actualFreq: Double = 1.0 / actualPeriod

    val ci: Interval = Interval(
        1.0 / (actualPeriod + std),
        1.0 / (actualPeriod - std)
    )

    val errorInCents: Double by lazy {
        // f * r**x = y
        // log(f) + x log(r) = log(y)
        // x = (log(y) - log(f)) / log(r)
        log(actualFreq, PitchHelper.centRatio) - log(expected.freq, PitchHelper.centRatio)
    }
}

data class Interval(val low: Double, val high: Double) {
    val size: Double by lazy { high - low }
}
