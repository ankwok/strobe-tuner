package com.akwok.strobetuner.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akwok.strobetuner.io.AudioData
import com.akwok.strobetuner.io.MicReader
import com.akwok.strobetuner.io.StorageWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SampleModel : ViewModel() {

    private val micReader = MicReader()
    private val sampleSize = 2048
    private val audioData = AudioData(FloatArray(sampleSize), MicReader.sampleRateInHz)

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
                val data = micReader.read(audioData).dat
                val text = data.joinToString("\n")
                writer.write(text)
                writer.write("\n")
            }
        }
    }

}