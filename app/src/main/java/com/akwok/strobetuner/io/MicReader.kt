package com.akwok.strobetuner.io

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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

    val isRecording: Boolean
        get() = audioRecorder.recordingState == AudioRecord.RECORDSTATE_RECORDING

    override fun read(buf: AudioData): AudioData {
        audioRecorder.read(buf.dat, 0, buf.dat.size, AudioRecord.READ_BLOCKING)
        return buf
    }

    companion object {
        const val sampleRateInHz = 44100
    }
}