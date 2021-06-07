package com.akwok.strobetuner.tuner

import kotlin.math.sign

class PitchDetector(ref: Double, sampleRateInHz: Int) {

    private val pitches = PitchHelper.getFrequencies(ref)
    private val dt = 1.0 / sampleRateInHz

    fun detect(audio: FloatArray): PitchError {
        val zeros = (0 until (audio.size - 1))
            .asSequence()
            .map { i -> Point(dt * i, audio[i].toDouble()) to Point(dt * (i + 1), audio[i + 1].toDouble()) }
            .filter { pair -> sign(pair.first.x) != sign(pair.second.x) && pair.first.x != 0.0 } // Corner case where something is exactly zero
            .map { pair ->
                // y - y1 = m(x - x1)
                // zero crossing ==> -y1 = m(x - x1) ==> x = x1 - y1/m
                val slope = (pair.second.x - pair.first.x) / dt
                pair.first.t - pair.first.x / slope
            }

        val avgPeriod = 2 * zeros // twice because there are (hopefully only) two zero crossings per period
            .zipWithNext { first, second -> second - first }
            .average()
        val avgFreq = 1.0 / avgPeriod

        val closestPitch = findClosestPitch(avgFreq)

        return PitchError(closestPitch, avgFreq)
    }

    fun findClosestPitch(freq: Double): Pitch {
        var left = 0
        var right = pitches.size

        while (right - left > 1) {
            val mid = (left + right) / 2
            val midFreq = pitches[mid].freq
            if (midFreq < freq) {
                left = mid
            } else {
                right = mid
            }
        }

        if (right == pitches.size) {
            return pitches[pitches.size - 1]
        } else if (freq - pitches[left].freq < pitches[right].freq - freq) {
            return pitches[left]
        }
        return pitches[right]
    }

    data class Point(val t: Double, val x: Double)
}