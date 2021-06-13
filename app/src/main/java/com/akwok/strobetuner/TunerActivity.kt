package com.akwok.strobetuner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.akwok.strobetuner.models.TunerModel
import com.akwok.strobetuner.tuner.PitchError
import com.akwok.strobetuner.tuner.PitchHelper
import java.time.LocalDateTime
import java.time.ZoneOffset

class TunerActivity : AppCompatActivity() {
    private val REQUEST_MIC: Int = 0

    private var clickCount = 0
    private var clickStart = 0L
    private val clickStartTtl = 5L
    private val clicksToSample = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tuner)

        getMicPermission()
        setupTextUpdater()
        setupRefPicker()
    }

    override fun onResume() {
        super.onResume()

        if (!hasMicPermission()) {
            recreate() // TODO: This is bad from a UX point of view.
        }

        val model: TunerModel by viewModels()
        model.startRecording()
    }

    override fun onPause() {
        super.onPause()

        val model: TunerModel by viewModels()
        model.stopRecording()
    }

    fun onTunerClick(view: View) {
        val now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        if (now - clickStart > clickStartTtl) {
            clickStart = now
            clickCount = 1
        } else {
            clickCount++
        }

        if (clickCount >= clicksToSample) {
            gotoSample()
        }
    }

    private fun setupTextUpdater() {
        val obs = Observer<PitchError?> { err -> if (err != null) textUpdater(err) }
        val model: TunerModel by viewModels()
        model.pitchError.observe(this, obs)
    }

    private fun textUpdater(pitchError: PitchError) {
        val noteStr = "${pitchError.expected.pitch} ${pitchError.expected.octave}"
        val freq = String.format("%.1f", pitchError.actualFreq)
        val error = String.format("%+.1f", pitchError.deltaFreq)

        val textBox = findViewById<TextView>(R.id.textView)
        textBox.text = "$noteStr\n$freq\n$error"
    }

    private fun setupRefPicker() {
        val picker = findViewById<NumberPicker>(R.id.ref_picker)
        picker.minValue = 400
        picker.maxValue = 500

        val prefs = getPreferences(MODE_PRIVATE)
        picker.value = prefs.getInt(getString(R.string.reference_A), PitchHelper.defaultReference)

        val model: TunerModel by viewModels()
        picker.setOnValueChangedListener { _, _, newVal ->
            model.referenceA.postValue(newVal)

            val editor = prefs.edit()
            editor.putInt(getString(R.string.reference_A), newVal)
            editor.apply()
        }
    }

    private fun getMicPermission() {
        if (!hasMicPermission()) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_MIC)
        }
    }

    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun gotoSample() {
        val intent = Intent(this, SampleActivity::class.java)
        startActivity(intent)
    }
}