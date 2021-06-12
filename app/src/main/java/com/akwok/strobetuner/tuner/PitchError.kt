package com.akwok.strobetuner.tuner

data class PitchError(val expected: Pitch, val actualFreq: Double, val ci: Interval) {
    val deltaFreq: Double = actualFreq - expected.freq
}

data class Interval(val low: Double, val high: Double) {
    val size: Double by lazy { high - low }
}
