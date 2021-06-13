package com.akwok.strobetuner.tuner

enum class PitchName {
    C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B
}

data class Pitch(val pitch: PitchName, val octave: Int, val freq: Double) : Comparable<Pitch> {
    override fun compareTo(other: Pitch): Int = when {
        octave < other.octave -> -1
        octave > other.octave -> 1
        else -> pitch.ordinal.compareTo(other.pitch.ordinal)
    }

}