package com.akwok.strobetuner.tuner

data class PitchError(val expected: Pitch, val actualPeriod: Double, val std: Double) {
    val actualFreq: Double = 1.0 / actualPeriod

    val ci: Interval = Interval(
        1.0 / (actualPeriod + std),
        1.0 / (actualPeriod - std)
    )

    val deltaFreq: Double = actualFreq - expected.freq
}

data class Interval(val low: Double, val high: Double) {
    val size: Double by lazy { high - low }
}
