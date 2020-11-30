package com.evmg.musicscoreapp.service

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.evmg.musicscoreapp.model.Score
import com.evmg.musicscoreapp.objectparsing.SheetMusic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.collections.ArrayList

class ScoreDb(private val context: Context) {
    val appDirectory = { context.getExternalFilesDir("Scores") }

    companion object {
        private val listeners = hashMapOf<Any, () -> Unit>()
    }

    fun subscribe(obj: Any, callback: () -> Unit) {
        listeners[obj] = callback
    }

    fun unsuscribe(obj: Any) {
        listeners.remove(obj)
    }

    suspend fun saveScore(score: Score, midiBytes: ByteArray) : Boolean {
        return withContext(Dispatchers.IO) {
            val directory = File(score.dir)

            score.sheets?.forEachIndexed { index, sheetMusic ->
                val imageFile = sheetMusic.imageFile
                val newFileName = "$index.${imageFile.extension}"
                Files.move(imageFile.toPath(), File(directory, newFileName).toPath())
            }

            //Write midi
            val midiFile = File(directory, "audio.mid")
            midiFile.writeBytes(midiBytes)

            //Write metadata
            val metadataFile = File(directory, "meta.json")
            FileWriter(metadataFile).use {
                it.write(score.getMetadataJson())
            }


            ZipOutputStream(BufferedOutputStream(FileOutputStream(File(directory,"export.dscore")))
            ).use { out ->
                for (file in directory.listFiles { _, f -> !f.contains("export") }) {
                    FileInputStream(file).use { fi ->
                        BufferedInputStream(fi).use { origin ->
                            val entry = ZipEntry(file.name)
                            out.putNextEntry(entry)
                            origin.copyTo(out, 1024)
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "${score.title} added", Toast.LENGTH_SHORT).show()
                notifyListeners()
            }
            true
        }
    }

    private fun notifyListeners() {
        for (listener in listeners) {
            listener.value.invoke()
        }
    }

    fun generatePermanetDirectory(): Path {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return Files.createDirectory(appDirectory.invoke()!!.toPath().resolve("score_$timeStamp"))
    }

    fun moveScoreToPermanent(score: Score) {
        File(score.dir).copyRecursively(generatePermanetDirectory().toFile(), true)
        File(score.dir).deleteRecursively()
        notifyListeners()
    }

    fun getAllScores(): ArrayList<Score> {
        val files = appDirectory.invoke()?.listFiles()?.toList() ?: listOf<File>()

        val result = ArrayList<Score>()

        files.filter { File(it, "meta.json").exists() }.forEach { file ->
            val metadataFile = JSONObject(FileReader(File(file, "meta.json")).readText())

            result.add(
                Score( // summary
                    dir = file.absolutePath,
                    title = metadataFile["title"] as String,
                    tempo = metadataFile["tempo"] as Int,
                    instrument = metadataFile["instrument"] as Int,
                    sheets = null,
                    createDate = Date(metadataFile["createDate"] as Long).time
                )
            )
        }

        return result
    }

    fun getScore(absolutePath: String): Score? {
        val file = File(absolutePath)
        if (!file.exists()) return null
        val metadataFile = JSONObject(FileReader(File(file, "meta.json")).readText())
        return Score(
            dir = file.absolutePath,
            title = metadataFile["title"] as String,
            tempo = metadataFile["tempo"] as Int,
            instrument = metadataFile["instrument"] as Int,
            sheets = getSheets(file),
            createDate = Date(metadataFile["createDate"] as Long).time
        )
    }

    private fun getSheets(dir: File): List<SheetMusic> {
        return dir.listFiles { f -> f.extension == "jpg" } .map {
            SheetMusic(
                it.nameWithoutExtension.toInt(),
                it)
        }
    }

    fun deleteScore(dir: String) {
        val scoreFolder = File(dir)
        if(scoreFolder.exists()) {
            scoreFolder.deleteRecursively()
            Toast.makeText(context, "Deletion success", Toast.LENGTH_SHORT).show()
            notifyListeners()
        }
    }
}