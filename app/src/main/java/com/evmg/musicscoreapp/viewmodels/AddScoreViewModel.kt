package com.evmg.musicscoreapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.evmg.musicscoreapp.service.ScoreToMidiConverter
import com.evmg.musicscoreapp.model.Score
import com.evmg.musicscoreapp.objectparsing.SheetMusic
import com.evmg.musicscoreapp.service.ScoreDb
import java.io.File
import java.nio.file.Files
import java.nio.file.Files.createTempDirectory
import java.text.SimpleDateFormat
import java.util.*

class AddScoreViewModel(application: Application) : AndroidViewModel(application) {
    val scoreDb = ScoreDb(application)
    val draftDir = createTempDirectory(application.getExternalFilesDir("Temp")!!.toPath(), "score_")
    var destDir = scoreDb.generatePermanentDirectory()
    var title: String = ""
    var tempo: Int = 0
    var instrument: Int = 0
    val sheets = arrayListOf<SheetMusic>()

    companion object {
        val INSTRUMENTS_BY_NAME = mapOf(
            "Piano" to 1,
            "Rhodes" to 5,
            "Violin" to 41,
            "Viola" to 42,
            "Cello" to 43,
            "Contra Bass" to 44,
            "Strings" to 49,
            "Trumpet" to 57,
            "Trombone" to 57,
            "Tuba" to 59,
            "French Horn" to 61,
            "Soprano Sax" to 65,
            "Alto Sax" to 66,
            "Tenor Sax" to 67,
            "Oboe" to 69,
            "Clarinet" to 72,
            "Piccolo" to 73,
            "Flute" to 74,
            "Ocarina" to 80
        )
        val INSTRUMENTS_BY_ID = INSTRUMENTS_BY_NAME.entries.associateBy { it.value }.mapValues {
            it.value.key
        }
    }

    fun setOriginalScore(score: Score) {
        destDir.toFile().deleteRecursively()
        destDir = File(score.dir)
            .toPath()
        title = score.title
        tempo = score.tempo
        instrument = score.instrument
        addAndProcessSheet(*(score.sheets ?: listOf())
            .map {
                Files.move(it.imageFile.toPath(), draftDir.resolve(it.imageFile.name)).toFile()
            }
            .toTypedArray())
    }

    fun getTempoString(): String {
        return if (tempo == 0) "" else tempo.toString()
    }

    fun setTempoString(value: String) {
        tempo = value.toIntOrNull() ?: 120
    }

    fun setInstrumentName(value: String) {
        instrument = INSTRUMENTS_BY_NAME[value] ?: error("InstrumentId not found for $value")
    }

    fun getInstrumentName(): String {
        return INSTRUMENTS_BY_ID[instrument] ?: ""
//        return INSTRUMENTS_BY_ID[instrument] ?: error("InstrumentName not found for $instrument")
    }

    fun createScore() {
        val score = Score(destDir.toFile().absolutePath, title,  tempo, instrument, sheets)
        ScoreToMidiConverter.saveScore(score, getApplication<Application>().applicationContext)
    }

    fun addAndProcessSheet(vararg imageFiles: File) {
        for (file in imageFiles) {
            val sheetID = ScoreToMidiConverter.processSheet(
                file,
                getApplication<Application>().applicationContext
            )
            sheets.add(SheetMusic(sheetID, file))
        }
    }

    fun getNewImageFile(): File {
        // Create an image file name

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(
            draftDir.toFile(),
            "JPEG_${timeStamp}.jpg"
        )
    }

    fun removeSheet(position: Int) {
        ScoreToMidiConverter.removeSheet(sheets[position].id)
        sheets.removeAt(position)
    }

}