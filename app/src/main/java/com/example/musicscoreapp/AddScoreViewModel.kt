package com.example.musicscoreapp

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.example.musicscoreapp.service.ScoreToMidiConverter
import com.example.musicscoreapp.utils.Score
import java.io.File

class AddScoreViewModel(application: Application) : AndroidViewModel(application) {
    var title: String = ""
    var tempo: Int = 0
    var instrument: Int = 0
    val sheets = arrayListOf<SheetMusic>()

    val instruments = mapOf(
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

    fun getTempoString(): String {
        return if (tempo == 0) "" else tempo.toString()
    }

    fun setTempoString(value: String) {
        tempo = value.toIntOrNull() ?: 120
    }

    fun setInstrumentName(value: String) {
        instrument = instruments[value] ?: error("")
    }

    fun createScore() {
        val score = Score(title,  tempo, instrument, sheets)
        ScoreToMidiConverter.saveScore(score, getApplication<Application>().applicationContext)
    }

    fun addSheet(image: Bitmap, imageFile: File) {
        val sheetID = ScoreToMidiConverter.processSheet(
            image,
            getApplication<Application>().applicationContext
        )
        sheets.add(SheetMusic(sheetID, image, imageFile))
    }

    fun removeSheet(position: Int) {
        ScoreToMidiConverter.removeSheet(sheets[position].id)
        sheets.removeAt(position)
    }

}