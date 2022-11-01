package com.akwok.strobetuner

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.akwok.strobetuner.models.TunerModel
import com.akwok.strobetuner.tuner.PitchError
import com.akwok.strobetuner.views.SettingsFragment
import com.akwok.strobetuner.views.StrobeView
import kotlin.math.roundToInt

class TunerActivity : AppCompatActivity() {

    private lateinit var preferencesService: PreferencesService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tuner)

        preferencesService = PreferencesService(applicationContext)

        if (preferencesService.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setupTextUpdater()
        setupRefPicker()
        setupStrobe()
    }

    override fun onResume() {
        super.onResume()

        if (shouldRecreate()) {
            recreate()
        }

        startRecordingSound()
        setupRefPicker()
        setupThreshold()

        val strobe = findViewById<StrobeView>(R.id.strobe_view)
        strobe.start()
    }

    private fun shouldRecreate(): Boolean {
        val visibility = if (preferencesService.shouldShowErr()) TextView.VISIBLE else TextView.GONE
        val errText = findViewById<TextView>(R.id.cents_error)

        return errText.visibility != visibility
    }

    private fun setupThreshold() {
        val model: TunerModel by viewModels()
        model.detectionThreshold.postValue(preferencesService.getDetectionThreshold())
    }

    override fun onPause() {
        super.onPause()

        val model: TunerModel by viewModels()
        model.stopRecording()

        val strobe = findViewById<StrobeView>(R.id.strobe_view)
        strobe.pause()
    }

    fun onSettingsClick(view: View) = SettingsActivity.gotoSettings(this)

    private fun setupTextUpdater() {
        val obs = Observer<PitchError?> { err -> if (err != null) textUpdater(err) }
        val model: TunerModel by viewModels()
        model.pitchError.observe(this, obs)

        val visibility = if (preferencesService.shouldShowErr()) TextView.VISIBLE else TextView.GONE
        val freqText = findViewById<TextView>(R.id.frequency)
        val errText = findViewById<TextView>(R.id.cents_error)
        freqText.visibility = visibility
        errText.visibility = visibility
    }

    private fun textUpdater(pitchError: PitchError) {
        val noteName =
            when (preferencesService.getNoteConvention()) {
                "solfege" -> pitchError.expected.pitch.solfegeName()
                else -> pitchError.expected.pitch.englishName()
            }
        val noteView = findViewById<TextView>(R.id.note_name)
        noteView.text = getString(R.string.note_name, noteName, pitchError.expected.octave)

        val freq = findViewById<TextView>(R.id.frequency)
        freq.text = getString(R.string.note_freq, pitchError.actualFreq)

        val centsErr = findViewById<TextView>(R.id.cents_error)
        centsErr.text = getString(R.string.cents_err, pitchError.errorInCents.roundToInt())

        val strobe = findViewById<StrobeView>(R.id.strobe_view)
        strobe.numBands = 2 * (pitchError.expected.octave + 1)
    }

    private fun setupRefPicker() {
        val picker = findViewById<NumberPicker>(R.id.ref_picker)
        picker.minValue = SettingsFragment.minRefFreq
        picker.maxValue = SettingsFragment.maxRefFreq

        val model: TunerModel by viewModels()

        val savedRef = preferencesService.getReferenceFreq()
        picker.value = savedRef
        model.referenceA.postValue(savedRef)

        picker.setOnValueChangedListener { _, _, newVal ->
            model.referenceA.postValue(newVal)
            preferencesService.setReferenceFreq(newVal)
        }
    }

    private fun setupStrobe() {
        val strobe = findViewById<StrobeView>(R.id.strobe_view)

        val obs = Observer<PitchError?> { err ->
            strobe.start()
            strobe.errorInCents = err?.errorInCents?.toFloat() ?: 0f
        }

        val model: TunerModel by viewModels()
        model.pitchError.observe(this, obs)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                recreate()
            } else {
                finishAndRemoveTask()
            }
        }

    private val micPermissionsDialog by lazy {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.mic_permissions_text))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            .setNegativeButton(getString(R.string.no)) { _, _ -> finishAndRemoveTask() }
            .create()
    }

    private fun startRecordingSound() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val model: TunerModel by viewModels()
            model.startRecording()
        } else {
            micPermissionsDialog.show()
        }
    }
}