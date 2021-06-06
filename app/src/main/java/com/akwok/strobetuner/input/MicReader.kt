package com.akwok.strobetuner.input

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.time.Duration
import kotlin.math.max

class MicReader : AudioReader {

    private val minBufferSize = AudioRecord
        .getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)

    private val bufferSize = max(minBufferSize, sampleRateInHz * 4 * 4)

    private val audioRecorder = AudioRecord.Builder()
        .setAudioFormat(AudioFormat.Builder()
            .setSampleRate(sampleRateInHz)
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
            .build())
        .setAudioSource(MediaRecorder.AudioSource.MIC)
        .setBufferSizeInBytes(bufferSize)
        .build()

    override fun startRecording() = audioRecorder.startRecording()

    override fun stopRecording() = audioRecorder.stop()

    override fun read(duration: Duration, buf: AudioData?): AudioData {
        val nSamples = (sampleRateInHz * duration.toMillis() / 1000).toInt()

        val thisBuf = buf ?: AudioData(FloatArray(nSamples), sampleRateInHz)
        require(thisBuf.dat.size == nSamples)

        audioRecorder.read(thisBuf.dat, 0, nSamples, AudioRecord.READ_BLOCKING)

        return thisBuf
    }

    companion object {
        const val sampleRateInHz = 44100
    }
}