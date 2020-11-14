package com.example.musicscoreapp.objectparsing

import android.graphics.RectF
import com.example.musicscoreapp.model.Recognition
import kotlin.math.round

interface MusicalObject {
    fun posX(): Float
}

class PitchContext (val barlineRef: RectF) {
    private enum class ClefValues(val delta: Int) {
        F(4),
        C(10),
        G(16)
    }

    private var clefVal = ClefValues.G;
    private val accidentalByPos = HashMap<Int,Int>()
    private val keyAccidental = IntArray(7)

    private val cMayorScale = intArrayOf(48, 50, 52, 53, 55, 57, 59)

    private fun getPos (mObject: RectF):Int {
        val oy = mObject.centerY()
        return round((this.barlineRef.bottom - oy) / (this.barlineRef.height() / 8)).toInt()
    }

    private fun getNote(pos: Int):Int {
        return pos + clefVal.delta
    }

    fun getPitch (mObject: RectF):Int {
        val pos = getPos(mObject)
        val note = getNote(pos)
        val naturalPitch = cMayorScale[note%7] + (note/7)*12

        return naturalPitch + (accidentalByPos[pos] ?: keyAccidental[note%7])
    }

    fun setClef(clef: Clef) {
        if (clef.clef.title == "clef.C") clefVal = ClefValues.C;
        if (clef.clef.title == "clef.F") clefVal = ClefValues.F;

        for (obj in clef.accidentals) {
            val note = getNote(getPos(obj.location))

            if (obj.title == "sign.flat") {
                keyAccidental[note % 7] = -1
            }
            if (obj.title == "sign.natural") {
                keyAccidental[note % 7] = 0
            }
            if (obj.title == "sign.sharp") {
                keyAccidental[note % 7] = 1
            }
        }
    }

    fun setAccidental(accidental: Accidental) {
        val pos = getPos(accidental.body.location)

        if (accidental.body.title == "sign.flat") {
            accidentalByPos[pos] = 0
        }
        if (accidental.body.title == "sign.natural") {
            accidentalByPos[pos] = 1
        }
        if (accidental.body.title == "sign.sharp") {
            accidentalByPos[pos] = -1
        }
    }

    fun process(musicalObject: MusicalObject) {
        if (musicalObject is Bar) accidentalByPos.clear();
        if (musicalObject is Clef) setClef(musicalObject)
        if (musicalObject is Accidental) setAccidental(musicalObject)
    }
}

class Accidental(val body: Recognition): MusicalObject {
    override fun posX() = body.location.left
}

class Bar(val bar: Recognition): MusicalObject {
    override fun posX() = bar.location.left
}

class Clef(val clef: Recognition, val accidentals: List<Recognition>): MusicalObject {
    override fun posX() = clef.location.left
}

enum class SymbolType(val factor: Float) {
    WHOLE(4F),
    HALF(2F),
    QUARTER(1F),
    EIGHT(0.5F),
    SIXTEENTH(0.25F)
}

class Note(val head: Recognition, val body: Recognition?, val dots: List<Recognition>): MusicalObject {
    fun getPitch(context: PitchContext): Int {
        return context.getPitch(head.location)
    }

    fun getDuration():Float {
        var duration = SymbolType.valueOf((body ?: head).title.split(".")[1].toUpperCase()).factor
        var totalDuration = duration
        for (i in dots.indices) {
            duration /= 2F
            totalDuration += duration
        }
        return totalDuration
    }

    override fun posX() = head.location.left
}

class Rest(val body: Recognition, val dots: List<Recognition>): MusicalObject {
    fun getDuration():Float {
        var duration = SymbolType.valueOf(body.title.split(".")[1]).factor
        var totalDuration = duration
        for (i in dots.indices) {
            duration /= 2F
            totalDuration += duration
        }
        return totalDuration
    }

    override fun posX() = body.location.left
}

object MusicalObjectFactory {
    fun parse(recognition: List<Recognition>): List<MusicalObject> {
        val result = mutableListOf<MusicalObject>()
        when {
            recognition.any { p -> p.title.startsWith("head") } -> {
                val head = recognition.first { it.title.startsWith("head") }
                val body = recognition.firstOrNull { it.title.startsWith("body") }
                val accidentals = recognition.filter { it.title.startsWith("sign") }.map { Accidental(it) }
                val dots = recognition.filter { it.title.startsWith("dot") }

                return listOf(Note(head, body, dots)) + accidentals
            }
            recognition.any { p -> p.title.startsWith("rest") } -> {
                val body = recognition.first { it.title.startsWith("rest") }
                val dots = recognition.filter { it.title.startsWith("dot") }

                return listOf(Rest(body, dots))
            }
            recognition.any { p -> p.title.startsWith("clef") } -> {
                val clef = recognition.first { it.title.startsWith("clef") }
                val accidentals = recognition.filter { it.title.startsWith("sign") }

                return listOf(Clef(clef, accidentals))
            }
            recognition.any { p -> p.title.startsWith("barline") } -> {
                return listOf(Bar(
                    bar = recognition.first {it.title.startsWith("barline")}
                ))
            }
            recognition.any { p -> p.title.startsWith("sign") } -> {
                return listOf(Accidental(
                    body = recognition.first {it.title.startsWith("sign")}
                ))
            }
            else -> return listOf()
        }
    }
}
