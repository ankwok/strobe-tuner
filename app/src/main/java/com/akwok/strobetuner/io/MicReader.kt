package com.akwok.strobetuner.io

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.max

class MicReader : AudioReader {

    private val audioRecorder = try {
        AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            bufferSize
        )
    } catch (e: SecurityException) {
        throw e // To make the IDE happy...
    }

    override fun startRecording() = audioRecorder.startRecording()

    override fun stopRecording() = audioRecorder.release()

    override fun read(buf: AudioData): AudioData {
        audioRecorder.read(buf.dat, 0, buf.dat.size, AudioRecord.READ_BLOCKING)
        return buf
    }

    override fun getBufferInstance(sampleSize: Int): AudioData =
        AudioData(FloatArray(sampleSize), sampleRateInHz)

    companion object {
        private const val sampleRateInHz = 44100
        private const val channelConfig = AudioFormat.CHANNEL_IN_MONO
        private const val audioFormat = AudioFormat.ENCODING_PCM_FLOAT

        private val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRateInHz, channelConfig, audioFormat)

        // 4 second buffer since each sample is a 4-byte float
        private val bufferSize = max(minBufferSize, sampleRateInHz * 4 * 4)
    }
}