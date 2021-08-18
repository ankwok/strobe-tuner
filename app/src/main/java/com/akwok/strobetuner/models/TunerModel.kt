package com.akwok.strobetuner.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akwok.strobetuner.io.AudioData
import com.akwok.strobetuner.io.MicReader
import com.akwok.strobetuner.tuner.PitchDetector
import com.akwok.strobetuner.tuner.PitchError
import com.akwok.strobetuner.tuner.PitchHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TunerModel : ViewModel() {
    private val micReaderInitializer = lazy { MicReader() }
    private val micReader: MicReader by micReaderInitializer
    private val sampleSize = 4096
    private var tuner = PitchDetector(PitchHelper.defaultReference.toDouble())
    private val audioData: AudioData = AudioData(FloatArray(sampleSize), MicReader.sampleRateInHz)

    fun startRecording() {
        micReader.startRecording()
        run()
    }

    fun stopRecording() {
        if (micReaderInitializer.isInitialized()) {
            micReader.stopRecording()
        }
    }

    val pitchError: MutableLiveData<PitchError?> by lazy {
        MutableLiveData<PitchError?>()
    }

    val referenceA: MutableLiveData<Int> by lazy {
        MutableLiveData(PitchHelper.defaultReference)
    }

    val detectionThreshold: MutableLiveData<Double> by lazy {
        MutableLiveData(PitchDetector.defaultDetectionThreshold)
    }

    private fun run() {
        viewModelScope.launch(Dispatchers.IO) {
            while (micReader.isRecording) {
                val ref = (referenceA.value ?: PitchHelper.defaultReference).toDouble()
                val threshold = detectionThreshold.value ?: PitchDetector.defaultDetectionThreshold

                if (ref != tuner.ref) {
                    tuner = PitchDetector(ref, threshold)
                } else if (threshold != tuner.detectionThreshold) {
                    tuner = PitchDetector(ref, threshold)
                }

                pitchError.postValue(tuner.detect(micReader.read(audioData)))
            }
        }
    }
}