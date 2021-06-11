package com.akwok.strobetuner.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akwok.strobetuner.io.AudioData
import com.akwok.strobetuner.io.MicReader
import com.akwok.strobetuner.io.StorageWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration

class SampleModel : ViewModel() {

    private val micReader = MicReader()
    private val readDuration = Duration.ofMillis(100)
    private var audioData: AudioData? = null

    fun startRecording() {
        micReader.startRecording()
        isRecording.postValue(true)
    }

    fun stopRecording() {
        micReader.stopRecording()
        isRecording.postValue(false)
    }

    val isRecording: MutableLiveData<Boolean> by lazy {
        MutableLiveData(false)
    }

    fun write(writer: StorageWriter) {
        viewModelScope.launch(Dispatchers.IO) {
            while (micReader.isRecording) {
                if (audioData == null || audioData!!.dat.size != micReader.getBufferSize(readDuration)) {
                    audioData = AudioData(FloatArray(micReader.getBufferSize(readDuration)), MicReader.sampleRateInHz)
                }
                val data = micReader.read(readDuration, audioData).dat
                val text = data.joinToString("\n")
                writer.write(text)
                writer.write("\n")
            }
        }
    }

}