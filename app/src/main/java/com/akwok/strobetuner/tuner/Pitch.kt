package com.akwok.strobetuner.tuner

enum class PitchName {
    C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B;

    fun englishName(): String = when (this) {
        C -> "C"
        C_SHARP -> "D♭"
        D -> "D"
        D_SHARP -> "E♭"
        E -> "E"
        F -> "F"
        F_SHARP -> "G♭"
        G -> "G"
        G_SHARP -> "A♭"
        A -> "A"
        A_SHARP -> "B♭"
        B -> "B"
    }

    fun solfegeName(): String = when (this) {
        C -> "Do"
        C_SHARP -> "Re♭"
        D -> "Re"
        D_SHARP -> "Mi♭"
        E -> "Mi"
        F -> "Fa"
        F_SHARP -> "Sol♭"
        G -> "Sol"
        G_SHARP -> "La♭"
        A -> "La"
        A_SHARP -> "Si♭"
        B -> "Si"
    }
}

data class Pitch(val pitch: PitchName, val octave: Int, val freq: Double) : Comparable<Pitch> {
    override fun compareTo(other: Pitch): Int = when {
        octave < other.octave -> -1
        octave > other.octave -> 1
        else -> pitch.ordinal.compareTo(other.pitch.ordinal)
    }

}