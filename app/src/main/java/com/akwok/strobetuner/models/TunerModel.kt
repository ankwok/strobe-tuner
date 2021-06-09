package com.akwok.strobetuner.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akwok.strobetuner.input.AudioData
import com.akwok.strobetuner.input.MicReader
import com.akwok.strobetuner.tuner.PitchDetector
import com.akwok.strobetuner.tuner.PitchError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration

class TunerModel : ViewModel() {
    private val refA = 440.0 // TODO: make this configurable
    private val micReader = MicReader()
    private val readDuration = Duration.ofMillis(100)
    private val tuner = PitchDetector(refA)
    private var audioData: AudioData? = null

    fun startRecording() {
        micReader.startRecording()
        run()
    }

    fun stopRecording() = micReader.stopRecording()

    val pitchError: MutableLiveData<PitchError> by lazy {
        MutableLiveData<PitchError>()
    }

    private fun run() {
        viewModelScope.launch(Dispatchers.IO) {
            while (micReader.isRecording) {
                if (audioData == null || audioData!!.dat.size != micReader.getBufferSize(readDuration)) {
                    audioData = AudioData(FloatArray(micReader.getBufferSize(readDuration)), MicReader.sampleRateInHz)
                }

                pitchError.postValue(tuner.detect(micReader.read(readDuration, audioData)))
            }
        }
    }
}