package com.evmg.musicscoreapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.*
import java.util.zip.ZipInputStream

class ImportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appDir = getExternalFilesDir("Scores")

        intent.data?.also {
            val hashMap = HashMap<String, ByteArray>()
            ZipInputStream(contentResolver.openInputStream(it)).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    hashMap[entry.name] = zip.readBytes()
                    zip.closeEntry()
                }
            }
            val obj = JSONObject(String(hashMap["meta.json"]!!))
            val directory = File(appDir, obj["title"] as String)
            if (directory.mkdir()) {
                for (e in hashMap) {
                    val nFile = File(directory, e.key)
                    FileOutputStream(nFile).use { it.write(e.value) }
                }
                contentResolver.openInputStream(it)?.copyTo(FileOutputStream(File(directory, "export.dscore")))
            }
        }
    }
}