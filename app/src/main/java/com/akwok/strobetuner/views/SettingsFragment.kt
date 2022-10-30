package com.akwok.strobetuner.views

import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.akwok.strobetuner.R
import com.akwok.strobetuner.tuner.PitchHelper

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val refAPref = findPreference<EditTextPreference>(getString(R.string.ref_A_pref))!!
        refAPref.title = getString(R.string.reference_A_title).format(minRefFreq, maxRefFreq)
        refAPref.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val ref = PreferenceManager
                .getDefaultSharedPreferences(requireContext())
                .getString(
                    getString(R.string.ref_A_pref),
                    PitchHelper.defaultReference.toString()
                )
                ?.toIntOrNull()
                ?: PitchHelper.defaultReference
            "$ref Hz"
        }
        refAPref.setOnBindEditTextListener { et ->
            et.inputType = InputType.TYPE_CLASS_NUMBER
        }
        refAPref.setOnPreferenceChangeListener { preference, newValue ->
            val intVal = newValue.toString().toIntOrNull() ?: -1
            intVal in minRefFreq..maxRefFreq
        }

        val noiseRejection = findPreference<SeekBarPreference>(getString(R.string.noise_rejection_pref))!!
        noiseRejection.max = noiseRejectionMaxValue

        val darkPref = findPreference<SwitchPreferenceCompat>(getString(R.string.dark_mode_pref))!!
        darkPref.setOnPreferenceChangeListener { _, newValue ->
            val isDark = newValue as Boolean
            if (isDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            true
        }
    }

    companion object {
        const val noiseRejectionMaxValue = 20
        const val minRefFreq = 400
        const val maxRefFreq = 500
    }
}