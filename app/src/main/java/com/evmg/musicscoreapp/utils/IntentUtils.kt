package com.evmg.musicscoreapp.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.evmg.musicscoreapp.model.Score
import java.io.File
import java.util.ArrayList

object IntentUtils {

    fun shareScoreMedia(context: Context, score: Score, filter: (f: File) -> Boolean) {
        val files = ArrayList(File(score.dir).listFiles { f: File -> filter(f) }.map {
            FileProvider.getUriForFile(
                context,
                "com.evmg.musicscoreapp.fileprovider",
                it
            )
        })
        if (files.size > 1) {
            val exportIntent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                type = "image/jpg|application/octet-stream"
            }
            context.startActivity(Intent.createChooser(exportIntent, "Send to other app"))
        } else if (files.size == 1) {
            val exportIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, files[0])
                type = "image/jpg|application/octet-stream"
            }
            context.startActivity(Intent.createChooser(exportIntent, "Send to other app"))
        } else {
            error("Cannot share 0 images")
        }

    }

}