package com.akwok.strobetuner.io

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedWriter
import java.io.OutputStreamWriter

class StorageWriter(private val ctx: Context, private val parentDir: Uri, private val displayName: String) {

    private lateinit var writer: BufferedWriter

    fun open(mimeType: String) {
        val fs = DocumentFile.fromTreeUri(ctx, parentDir)!!

        val maybeExisting = fs.findFile(displayName)
        maybeExisting?.delete()

        val questionsFile = fs.createFile(mimeType, displayName)
        val os = ctx.contentResolver.openOutputStream(questionsFile!!.uri)
        writer = BufferedWriter(OutputStreamWriter(os))
    }

    fun write(text: String) = writer.write(text)

    fun close() {
        writer.flush()
        writer.close()
    }
}