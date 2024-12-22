package com.akwok.simpletuner.tuner

import kotlin.math.pow

object PitchHelper {
    const val defaultReference: Int = 440
    val centRatio: Double = (2.0).pow(1.0 / 1200.0)

    private val lowestPitch = Pitch(PitchName.A, 0, Double.NaN)
    private val highestPitch = Pitch(PitchName.C, 8, Double.NaN)

    fun getFrequencies(ref: Double): List<Pitch> {
        val refOctave = 4
        val two = 2.0

        return ArrayList(  // Ensures that it supports random access
            (0 until 9)
                .flatMap { octave ->
                    val deltaOctave = octave - refOctave
                    val semiTones = 12 * deltaOctave - 9

                    PitchName.values()
                        .mapIndexed { i, pitch ->
                            val freq = ref * two.pow((semiTones.toDouble() + i) / 12)
                            Pitch(pitch, octave, freq)
                        }
                }
                .filter { pitch -> pitch in lowestPitch..highestPitch })
    }
}