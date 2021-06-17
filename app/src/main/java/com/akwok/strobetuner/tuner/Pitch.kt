package com.akwok.strobetuner.tuner

enum class PitchName {
    C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B;

    fun englishName(): String = when (this) {
        C -> "C"
        C_SHARP -> "C♯ / D♭"
        D -> "D"
        D_SHARP -> "D♯ / E♭"
        E -> "E"
        F -> "F"
        F_SHARP -> "F♯ / G♭"
        G -> "G"
        G_SHARP -> "G♯ / A♭"
        A -> "A"
        A_SHARP -> "A♯ / B♭"
        B -> "B"
    }

    fun solfegeName(): String = when (this) {
        C -> "Do"
        C_SHARP -> "Do♯ / Re♭"
        D -> "Re"
        D_SHARP -> "Re♯ / Mi♭"
        E -> "Mi"
        F -> "Fa"
        F_SHARP -> "Fa♯ / Sol♭"
        G -> "Sol"
        G_SHARP -> "Sol♯ / La♭"
        A -> "La"
        A_SHARP -> "La♯ / Si♭"
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