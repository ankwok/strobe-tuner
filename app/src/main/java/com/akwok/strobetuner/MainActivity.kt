package com.akwok.strobetuner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.akwok.strobetuner.input.AudioData
import com.akwok.strobetuner.input.MicReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.time.Duration

class MainActivity : AppCompatActivity() {
    private val REQUEST_MIC: Int = 0
    private val WRITE: Int = 1

    private var rec: MicReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_MIC)
        } else {
            initRec()
        }
    }

    fun goButton(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }

        startActivityForResult(intent, WRITE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode){
            REQUEST_MIC -> initRec()
            WRITE ->recordAndWriteFloats(data!!.data!!)
        }
    }

    private fun initRec() {
        rec = MicReader()
    }

    fun recordAndWriteFloats(uri: Uri) {
        rec!!.startRecording()
        var buf: AudioData? = null
        (0 until 20)
            .forEach { i ->
                val audio = rec!!.read(Duration.ofMillis(1000), buf)
                val floatsStr = audio.dat.joinToString("\n") { x -> x.toString() }
                write(floatsStr, uri, "micData_$i", "text/csv", true)
                buf = audio
            }
        rec!!.stopRecording()
    }

    fun write(
        text: String,
        parentDir: Uri,
        displayName: String,
        mimeType: String,
        overwrite: Boolean = false
    ): Boolean {
        val fs = DocumentFile.fromTreeUri(applicationContext, parentDir)!!

        val maybeExisting = fs.findFile(displayName)
        if (!overwrite && maybeExisting != null) {
            return false
        } else {
            maybeExisting?.delete()
        }

        val questionsFile = fs.createFile(mimeType, displayName)
        val os = contentResolver.openOutputStream(questionsFile!!.uri)
        val writer = BufferedWriter(OutputStreamWriter(os))
        writer.write(text)
        writer.flush()
        writer.close()

        return true
    }
}