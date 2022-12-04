package com.akwok.strobetuner.tuner

import com.akwok.strobetuner.io.AudioData
import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.max
import kotlin.math.pow

class PitchDetector(val ref: Double, val detectionThreshold: Double = defaultDetectionThreshold) {

    private val pitches = PitchHelper.getFrequencies(ref)
    private val smallestPeriod = 1.0 / (pitches.last().freq * PitchHelper.centRatio.pow(49))
    private val longestPeriod = 1.0 / (pitches.first().freq * PitchHelper.centRatio.pow(-50))

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
            val qParam = getQParameter(audioDat.dat.size, audioDat.sampleRate, measurement)
            kalmanFilter = KalmanUpdater(
                KalmanState(measurement.actualPeriod, measurement.variance), qParam)
            currentPitch = measurement.expected
        } else {
            kalmanFilter!!.update(measurement.actualPeriod, measurement.variance)
        }

        return PitchError(
            measurement.expected,
            kalmanFilter!!.stateEstimate.x,
            kalmanFilter!!.stateEstimate.P)
    }

    private fun autocorrDetect(audioDat: AudioData): PitchError? {
        val autocorr = autocorrelate(audioDat.dat)
        val valid = autocorr.sliceArray(IntRange(0, autocorr.size / 2))

        val maxVal = valid.maxOrNull()!!
        val dx = maxVal / gridSearchNum

        val bestGuess = (0 until gridSearchNum)
            .map { i ->
                computePeriodFromZeroCrossings(
                    AudioData(valid, audioDat.sampleRate),
                    (i * dx).toDouble()
                )
            }
            .filter { moment -> moment.mean.isFinite() && moment.mean in smallestPeriod..longestPeriod }
            .map { moment ->
                val avgFreq = 1.0 / moment.mean
                val closestPitch = findClosestPitch(avgFreq)
                PitchError(closestPitch, moment.mean, moment.variance)
            }
            .minWithOrNull(pitchErrorComparator)

        return bestGuess
    }

    private fun computePeriodFromZeroCrossings(audioDat: AudioData, offset: Double): Moments {
        val dt = 1.0 / audioDat.sampleRate
        val audio = audioDat.dat

        val zeros = emptyList<Double>().toMutableList()
        for (i in 0 until (audio.size - 1)) {
            val t1 = dt * i
            val x1 = audio[i].toDouble()
            val x2 = audio[i + 1].toDouble()

            if (x1 > offset && x2 <= offset) {
                // y - y1 = m(x - x1)
                // ==> x = (y - y1) / m + x1
                val slope = (x2 - x1) / dt
                zeros.add((offset - x1) / slope + t1)
            }
        }

        if (zeros.isEmpty()) {
            return Moments(Double.NaN, Double.NaN)
        }

        val deltas = List(zeros.size - 1) { i -> zeros[i + 1] - zeros[i] }
        val avg = deltas.average()

        var variance = 0.0
        for (del in deltas) {
            val diff = del - avg
            variance += diff * diff
        }
        variance /= max(deltas.size - 1, 1) // Use unbiased variance estimate if possible

        return Moments(avg, variance)
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

    companion object {
        private const val gridSearchNum = 5
        const val defaultDetectionThreshold: Double = 0.1
        const val maxDetectionThreshold = 0.4
        private const val driftInCentsPerSecond = 0.5

        private fun getQParameter(sampleSize: Int, sampleRate: Int, measurement: PitchError): Double {
            val durationSeconds = sampleSize.toDouble() / sampleRate

            // freq = 1 / period
            // freq * centRatio^(drift) = 1 / period_2
            // period - period_2 = (1 / freq - 1 / (freq * centRatio^(drift)))
            //                   = 1 / freq * (1 - centRatio^(-drift))
            //                   = period * (1 - centRatio^(-drift))
            val driftPeriod = measurement.actualPeriod * (
                    1.0 - PitchHelper.centRatio.pow(-driftInCentsPerSecond))
            return driftPeriod * driftPeriod * durationSeconds * durationSeconds
        }

        private val pitchErrorComparator =
            compareBy<PitchError> { it.expected }.thenBy { it.variance }
    }
}