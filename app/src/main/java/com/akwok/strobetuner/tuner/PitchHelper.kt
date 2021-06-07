package com.akwok.strobetuner.tuner

import kotlin.math.pow

object PitchHelper {
    val halfnoteRatio: Double = (2.0).pow(1.0 / 12.0)
    val centRatio: Double = (2.0).pow(1.0 / 1200.0)

    fun getFrequencies(ref: Double): List<Pitch> {
        val refOctave = 4
        val two = 2.0

        return (0 until 9)
            .flatMap { octave ->
                val deltaOctave = octave - refOctave
                val semiTones = 12 * deltaOctave - 9

                PitchName.values()
                    .mapIndexed { i, pitch ->
                        val freq = ref * two.pow((semiTones.toDouble() + i) / 12)
                        Pitch(pitch, octave, freq)
                    }
            }
    }
}