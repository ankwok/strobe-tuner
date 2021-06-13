package com.akwok.strobetuner.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akwok.strobetuner.io.AudioData
import com.akwok.strobetuner.io.MicReader
import com.akwok.strobetuner.tuner.PitchDetector
import com.akwok.strobetuner.tuner.PitchError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TunerModel : ViewModel() {
    private val refA = 440.0 // TODO: make this configurable
    private val micReader = MicReader()
    private val sampleSize = 4096
    private val tuner = PitchDetector(refA)
    private val audioData: AudioData = AudioData(FloatArray(sampleSize), MicReader.sampleRateInHz)

    fun startRecording() {
        micReader.startRecording()
        run()
    }

    fun stopRecording() = micReader.stopRecording()

    val pitchError: MutableLiveData<PitchError?> by lazy {
        MutableLiveData<PitchError?>()
    }

    private fun run() {
        viewModelScope.launch(Dispatchers.IO) {
            while (micReader.isRecording) {
                pitchError.postValue(tuner.detect(micReader.read(audioData)))
            }
        }
    }
}