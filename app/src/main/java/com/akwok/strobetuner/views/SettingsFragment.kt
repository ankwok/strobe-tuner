package com.akwok.strobetuner.views

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.akwok.strobetuner.R
import com.akwok.strobetuner.tuner.PitchHelper

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val refAPref = findPreference<EditTextPreference>(getString(R.string.ref_A_pref))!!
        refAPref.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val ref = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(getString(R.string.ref_A_pref), PitchHelper.defaultReference.toString())!!
            "$ref Hz"
        }
        refAPref.setOnBindEditTextListener { et ->
            et.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }
}