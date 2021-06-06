package com.akwok.strobetuner.input

data class AudioData(val dat: FloatArray, val sampleRate: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioData

        if (!dat.contentEquals(other.dat)) return false
        if (sampleRate != other.sampleRate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dat.contentHashCode()
        result = 31 * result + sampleRate
        return result
    }
}
