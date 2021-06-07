package com.akwok.strobetuner.tuner

import org.junit.Assert.*
import org.junit.Test

class PitchHelperUnitTest {

    @Test
    fun referencePitchInPitches() {
        val pitches = PitchHelper.getFrequencies(440.0)
        assertTrue(pitches.contains(Pitch(PitchName.A, 4, 440.0)))
    }

    @Test
    fun pitchesAreOrdered() {
        val pitches = PitchHelper.getFrequencies(440.0)
        assertEquals(pitches.sortedBy { p -> p.freq }, pitches)
    }

    @Test
    fun thereAreNineOctaves() {
        val pitches = PitchHelper.getFrequencies(440.0)
        assertEquals(9 * 12, pitches.size)
    }

    @Test
    fun c0IsFirst() {
        val pitches = PitchHelper.getFrequencies(440.0)
        val first = pitches.first()

        assertEquals(PitchName.C, first.pitch)
        assertEquals(0, first.octave)
        assertEquals(16.35, first.freq, 1e-2)
    }
}