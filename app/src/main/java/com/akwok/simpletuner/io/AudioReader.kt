package com.akwok.simpletuner.io

interface AudioReader {
    fun startRecording()
    fun stopRecording()
    fun read(buf: AudioData): AudioData
    fun getBufferInstance(sampleSize: Int): AudioData
}