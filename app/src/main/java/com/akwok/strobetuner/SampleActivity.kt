package com.akwok.strobetuner

import android.app.Activity
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.akwok.strobetuner.io.StorageWriter
import com.akwok.strobetuner.models.SampleModel

class SampleActivity : AppCompatActivity() {

    private lateinit var recordLauncher: ActivityResultLauncher<Intent>
    private lateinit var writer: StorageWriter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        val model: SampleModel by viewModels()
        val obs = Observer<Boolean> { bool -> updateRecordColor(bool) }
        model.isRecording.observe(this, obs)

        recordLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val fileDir = result.data!!.data!!

                model.startRecording()

                val fileName = "audio.csv"
                writer = StorageWriter(applicationContext, fileDir, fileName)
                writer.open("text/csv")
                model.write(writer)
            }
        }

    }

    private fun updateRecordColor(isRecording: Boolean) {
        val btn = findViewById<Button>(R.id.recordButton)
        val color = if (isRecording) Color.RED else Color.DKGRAY

        if (Build.VERSION.SDK_INT >= 29) {
            btn.background.colorFilter = BlendModeColorFilter(color, BlendMode.MULTIPLY)
        } else {
            btn.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }
    }

    fun onRecordButtonClick(view: View) {
        val model: SampleModel by viewModels()

        if (model.isRecording.value == true) {
            writer.close()
            model.stopRecording()
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            }
            recordLauncher.launch(intent)
        }
    }

    fun gotoTuner(view: View) {
        val intent = Intent(this, TunerActivity::class.java)
        startActivity(intent)
    }

}