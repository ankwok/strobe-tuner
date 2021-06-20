package com.akwok.strobetuner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    companion object {
        fun gotoSettings(from: Activity) {
            val intent = Intent(from.applicationContext, SettingsActivity::class.java)
            from.startActivity(intent)
        }
    }
}