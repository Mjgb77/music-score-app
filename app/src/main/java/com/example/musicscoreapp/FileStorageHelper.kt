package com.example.musicscoreapp

import android.content.Context
import android.os.FileUtils
import android.widget.Toast
import androidx.core.net.toFile
import com.example.musicscoreapp.utils.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class FileStorageHelper(private val context: Context) {
   init {
        appDirectory = context.getExternalFilesDir("Scores")
   }

    suspend fun addScore(score: Score, midiBytes: ByteArray) : Boolean{
        val path = File(appDirectory, score.title)

        //Create the directory
        val dirCreated = path.mkdir()

        return if(dirCreated){
            score.sheets.forEachIndexed { index, sheetMusic ->
                val imageFile = sheetMusic.imageFile
                val newFileName = "$index.${imageFile.extension}"
                Files.move(imageFile.toPath(), File(path,newFileName).toPath())
            }
            //Write midi
            val midiFile = File(path,"audio.mid")
            midiFile.writeBytes(midiBytes)

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "${score.title} added", Toast.LENGTH_SHORT).show()
            }
            true
        }else{
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Insertion failed", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    fun getAllScores(): ArrayList<MusicScore> {
        val files = appDirectory?.list();

        val result = ArrayList<MusicScore>()

        files?.forEach { file ->
            val item = MusicScore()
            item.title = file
            item.path = File(appDirectory,file).path
            result.add(item)
        }

        return result;
    }

    fun getScoreCount(): Int {
        return appDirectory?.list()?.size as Int
    }

    fun getByTitle(title: String?): MusicScore? {
        if(title == null) return null
        val file = File(appDirectory,title)
        if(file.exists()){
            val result = MusicScore()
            result.title = title
            result.path = file.path;
            return result
        }

      return null
    }

    fun deleteScore(title: String?) {
        if(title == null) return

        val scoreFolder = File(appDirectory,title)
        if(scoreFolder.exists()) {
            scoreFolder.deleteRecursively()
            Toast.makeText(context, "Deletion success", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
       private var appDirectory: File? = null
    }
}