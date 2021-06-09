package com.akwok.strobetuner.tuner

data class PitchError(val expected: Pitch, val actualFreq: Double) {
    val deltaFreq: Double = actualFreq - expected.freq
}
