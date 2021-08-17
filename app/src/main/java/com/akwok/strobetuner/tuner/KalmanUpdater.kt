package com.akwok.strobetuner.tuner

/**
 * One-dimensional Kalman filter with no process and measurement dynamics and time-invariant
 * process noise.
 *
 * x_{k + 1} = x_k + w_k
 * y_k = x_k + v_k
 * w_k ~ Q and v_k ~ R
 *
 * K = (P_k + Q) / (P_k + Q + R)
 * P_{k + 1} = (1 - K)(P_k + Q)
 * xHat_{k|k} = xHat_{k-1|k-1} + K (y_k - xHat_{k-1|k-1})
 */
class KalmanUpdater(x0: KalmanState, private val Q: Double) {

    var stateEstimate: KalmanState = x0
        private set

    fun update(measurement: Double, variance: Double): KalmanState {
        val predictionVariance = stateEstimate.P + Q
        val innovation = measurement - stateEstimate.x
        val kalmanGain = predictionVariance / (predictionVariance + variance)

        stateEstimate = KalmanState(
            stateEstimate.x + kalmanGain * innovation,
            (1 - kalmanGain) * predictionVariance)

        return stateEstimate
    }
}

data class KalmanState(val x: Double, val P: Double)