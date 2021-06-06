package com.akwok.strobetuner.input

import java.time.Duration

interface AudioReader {
    fun startRecording()
    fun stopRecording()
    fun read(duration: Duration, buf: AudioData?): AudioData
}