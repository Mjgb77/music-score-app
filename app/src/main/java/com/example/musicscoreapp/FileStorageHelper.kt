package com.example.musicscoreapp

import android.content.Context
import android.widget.Toast
import java.io.File

class FileStorageHelper(private val context: Context) {
   init {
        appDirectory = context.getExternalFilesDir("Scores")
   }

    fun addScore(title: String) : Boolean{
        val path = File(appDirectory, title)

        //Create the directory
        val dirCreated = path.mkdir()

        return if(dirCreated){
            Toast.makeText(context, "$title added", Toast.LENGTH_SHORT).show()
            true
        }else{
            Toast.makeText(context,  "Insertion failed", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun getAllScores(): ArrayList<MusicScore> {
        val files = appDirectory?.list();

        val result = ArrayList<MusicScore>()

        files?.forEach { file ->
            val item = MusicScore()
            item.title = file
            result.add(item)
        }

        return result;
    }

    fun getScoreCount(): Int {
        return appDirectory?.list()?.size as Int
    }

    fun getByTitle(title: String?): MusicScore? {
        if(title == null) return null

        if(File(appDirectory,title).exists()){
            val result = MusicScore()
            result.title = title
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