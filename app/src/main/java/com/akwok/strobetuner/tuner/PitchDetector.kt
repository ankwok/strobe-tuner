package com.akwok.strobetuner.tuner

import com.akwok.strobetuner.io.AudioData
import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.sqrt

class PitchDetector(val ref: Double, val detectionThreshold: Double = defaultDetectionThreshold) {

    private val pitches = PitchHelper.getFrequencies(ref)
    private val gridSearchNum = 5

    private var kalmanFilter: KalmanUpdater? = null
    private var currentPitch: Pitch? = null

    fun detect(audioDat: AudioData): PitchError? {
        return detectWithKalmanFilter(audioDat)
    }

    private fun detectWithKalmanFilter(audioDat: AudioData): PitchError? {
        if (audioDat.dat.maxOrNull().let { x -> x == null || x <= detectionThreshold }) {
            return null
        }

        val measurement = autocorrDetect(audioDat) ?: return null

        if (currentPitch != measurement.expected)
        {
            val halfCentPeriod = measurement.actualPeriod * (1 - 1.0 / sqrt(PitchHelper.centRatio))
            kalmanFilter = KalmanUpdater(
                KalmanState(measurement.actualPeriod, measurement.std * measurement.std),
                halfCentPeriod * halfCentPeriod)
            currentPitch = measurement.expected
        } else {
            kalmanFilter!!.update(measurement.actualPeriod, measurement.std * measurement.std)
        }

        return PitchError(
            measurement.expected,
            kalmanFilter!!.stateEstimate.x,
            sqrt(kalmanFilter!!.stateEstimate.P))
    }

    private fun autocorrDetect(audioDat: AudioData): PitchError? {
        val autocorr = autocorrelate(audioDat.dat)
        val valid = autocorr.sliceArray(IntRange(0, autocorr.size / 2))

        val maxVal = valid.maxOrNull()!!
        val dx = maxVal / gridSearchNum

        val err = (0 until gridSearchNum)
            .map { i ->
                val offset = i * dx
                val avgPeriod = computePeriodFromZeroCrossings(AudioData(valid, audioDat.sampleRate), offset.toDouble())
                val avgFreq = 1.0 / avgPeriod.mean
                val closestPitch = findClosestPitch(avgFreq)
                PitchError(closestPitch, avgPeriod.mean, avgPeriod.std)
            }
            .minByOrNull { err -> PitchErr(err.expected, err.ci) }!!

        return if (err.actualPeriod.isFinite()) err else null
    }

    private fun computePeriodFromZeroCrossings(audioDat: AudioData, offset: Double): MeanStd {
        val dt = 1.0 / audioDat.sampleRate
        val audio = audioDat.dat

        val zeros = (0 until (audio.size - 1))
            .asSequence()
            .map { i -> Point(dt * i, audio[i].toDouble()) to Point(dt * (i + 1), audio[i + 1].toDouble()) }
            .filter { pair -> pair.first.x > offset && pair.second.x <= offset } // get crossings from above
            .map { pair ->
                // y - y1 = m(x - x1)
                // ==> x = (y - y1) / m + x1
                val slope = (pair.second.x - pair.first.x) / dt
                (offset - pair.first.x) / slope + pair.first.t
            }

        val deltas = zeros
            .zipWithNext { first, second -> second - first }
            .toList()
        val avg = deltas.average() // twice because there are (hopefully only) two zero crossings per period
        val variance = deltas
            .map { d -> (d - avg) * (d - avg) }
            .average()

        return MeanStd(avg, sqrt(variance))
    }

    private fun autocorrelate(audio: FloatArray): FloatArray {
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

    private data class Point(val t: Double, val x: Double)

    private data class PitchErr(val pitch: Pitch, val ci: Interval) : Comparable<PitchErr> {
        override fun compareTo(other: PitchErr): Int = when {
            pitch < other.pitch -> -1
            pitch > other.pitch -> 1
            else -> ci.size.compareTo(other.ci.size)
        }

    }

    companion object {
        const val defaultDetectionThreshold: Double = 0.1
        const val maxDetectionThreshold = 0.4
    }
}