package com.akwok.strobetuner.models

import android.util.Log
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
    private val micReader = MicReader()
    private val sampleSize = 4096
    private var tuner = PitchDetector(PitchHelper.defaultReference.toDouble())
    private val audioData: AudioData = AudioData(FloatArray(sampleSize), MicReader.sampleRateInHz)

    fun startRecording() {
        micReader.startRecording()
        run()
    }

    fun stopRecording() = micReader.stopRecording()

    val pitchError: MutableLiveData<PitchError?> by lazy {
        MutableLiveData<PitchError?>()
    }

    val referenceA: MutableLiveData<Int> by lazy {
        MutableLiveData(PitchHelper.defaultReference)
    }

    private fun run() {
        viewModelScope.launch(Dispatchers.IO) {
            while (micReader.isRecording) {
                val ref = (referenceA.value ?: PitchHelper.defaultReference).toDouble()
                if (ref != tuner.ref) {
                    Log.d(this::class.simpleName, "Reference A changed from ${tuner.ref} to $ref")
                    tuner = PitchDetector(ref)
                }

                pitchError.postValue(tuner.detect(micReader.read(audioData)))
            }
        }
    }
}