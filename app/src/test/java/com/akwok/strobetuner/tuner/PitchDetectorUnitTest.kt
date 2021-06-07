package com.akwok.strobetuner.tuner

import org.junit.Assert
import org.junit.Test
import kotlin.math.sin
import kotlin.math.PI

class PitchDetectorUnitTest {

    private val sampleRate = 44100
    private val pitches = PitchHelper.getFrequencies(440.0)

    private val findClosestPitchCases = listOf(
        arrayOf(439.0, Pitch(PitchName.A, 4, 440.0)),
        arrayOf(440.0, Pitch(PitchName.A, 4, 440.0)),
        arrayOf(441.0, Pitch(PitchName.A, 4, 440.0)),
        arrayOf(20000.0, pitches.last()),
        arrayOf(0.0, pitches.first()),
        arrayOf(0.5 * (pitches[10].freq + pitches[11].freq), pitches[11])   // Round up
    )

    @Test
    fun findClosestPitch() {
        val detector = PitchDetector(440.0, sampleRate)

        findClosestPitchCases
            .forEach { case ->
                val freq = case[0] as Double
                val expected = case[1] as Pitch
                val actual = detector.findClosestPitch(freq)
                Assert.assertEquals("Test case ($freq, $expected)", expected, actual)
            }
    }

    private val detectCases = listOf(
        arrayOf(439.0, Pitch(PitchName.A, 4, 440.0)),
        arrayOf(440.0, Pitch(PitchName.A, 4, 440.0)),
        arrayOf(441.0, Pitch(PitchName.A, 4, 440.0))
    )

    private fun sineWave(freq: Double, offset: Double) = (0 until 4096)
        .map { i -> (sin(2 * PI * freq * i / sampleRate) + offset).toFloat() }
        .toFloatArray()

    private fun runDetection(detector: PitchDetector, sineGenerator: (Double) -> FloatArray) = detectCases
        .forEach { case ->
            val freq = case[0] as Double
            val expectedPitch = case[1] as Pitch

            val audio = sineGenerator(freq)
            val pitchError = detector.detect(audio)

            Assert.assertEquals("Test case ($freq, $expectedPitch)", expectedPitch, pitchError.expected)
            Assert.assertEquals("Test case ($freq, $expectedPitch)", freq, pitchError.actualFreq, 1e-3)
        }

    @Test
    fun detectBasicSine() {
        val detector = PitchDetector(440.0, sampleRate)
        val sineGenerator = { freq: Double -> sineWave(freq, 0.0) }
        runDetection(detector, sineGenerator = sineGenerator)
    }

    @Test
    fun detectOffsetSine() {
        val detector = PitchDetector(440.0, sampleRate)
        val sineGenerator = { freq: Double -> sineWave(freq, 0.2) }
        runDetection(detector, sineGenerator = sineGenerator)
    }
}