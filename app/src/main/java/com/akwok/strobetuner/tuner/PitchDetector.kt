package com.akwok.strobetuner.tuner

import com.akwok.strobetuner.io.AudioData
import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt

class PitchDetector(ref: Double) {

    private val pitches = PitchHelper.getFrequencies(ref)

    fun detect(audioDat: AudioData): PitchError = autocorrDetect(audioDat) //naiveDetect(audioDat)

    private fun naiveDetect(audioDat: AudioData): PitchError {
        val avgPeriod = computePeriodFromZeroCrossings(audioDat, 0.0)
        val avgFreq = 1.0 / avgPeriod.mean
        val closestPitch = findClosestPitch(avgFreq)
        return PitchError(closestPitch, avgFreq, getCI(avgPeriod))
    }

    private fun autocorrDetect(audioDat: AudioData): PitchError {
        val autocorr = autocorrelate(audioDat.dat)
        val valid = autocorr.sliceArray(IntRange(0, autocorr.size / 2))
        val maxVal = valid.maxOrNull()!!
        val avgPeriod = computePeriodFromZeroCrossings(AudioData(valid, audioDat.sampleRate), 0.8 * maxVal)
        val avgFreq = 1.0 / avgPeriod.mean
        val closestPitch = findClosestPitch(avgFreq)
        return PitchError(closestPitch, avgFreq, getCI(avgPeriod))
    }

    fun getCI(err: MeanStd): Interval {
        val low = 1.0 / (err.mean + err.std)
        val hi = 1.0 / (err.mean - err.std)
        return Interval(low, hi)
    }

    fun computePeriodFromZeroCrossings(audioDat: AudioData, offset: Double): MeanStd {
        val dt = 1.0 / audioDat.sampleRate
        val audio = audioDat.dat

        val zeros = (0 until (audio.size - 1))
            .asSequence()
            .map { i -> Point(dt * i, audio[i].toDouble()) to Point(dt * (i + 1), audio[i + 1].toDouble()) }
            .filter { pair -> sign(pair.first.x - offset) != sign(pair.second.x - offset) && pair.first.x != offset } // Corner case where something is exactly zero
            .map { pair ->
                // y - y1 = m(x - x1)
                // ==> x = (y - y1) / m + x1
                val slope = (pair.second.x - pair.first.x) / dt
                (offset - pair.first.x) / slope + pair.first.t
            }

        val deltas = zeros
            .zipWithNext { first, second -> second - first }
            .toList()
        val avg = 2 * deltas.average() // twice because there are (hopefully only) two zero crossings per period
        val variance = deltas
            .map { d -> (d - avg).pow(2) }
            .average()

        return MeanStd(avg, sqrt(variance))
    }

    fun autocorrelate(audio: FloatArray): FloatArray {
        val fft = FloatFFT_1D(audio.size.toLong())

        val window = FloatArray(audio.size)
        audio.copyInto(window, 0, 0, audio.size / 2)

        fft.realForward(audio)
        fft.realForward(window)

        JTransformsHelper.conj(window)
        val autocorr = JTransformsHelper.mult(audio, window)
        fft.realInverse(autocorr, false)

        return autocorr
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