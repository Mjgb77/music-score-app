package com.example.musicscoreapp.objectparsing

import android.graphics.RectF
import kotlin.math.round

class PitchContext (val barlineRef: RectF) {
    private enum class ClefValues(val delta: Int) {
        F(4),
        C(10),
        G(16)
    }

    private var clefVal = ClefValues.G
    private val accidentalByPos = HashMap<Int,Int>()
    private val keyAccidental = IntArray(7)

    private val cMayorScale = intArrayOf(48, 50, 52, 53, 55, 57, 59)

    private fun getPos (mObject: RectF):Int {
        val oy = mObject.centerY()
        return round((this.barlineRef.bottom - oy) / (this.barlineRef.height() / 8))
            .toInt()
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
        if (clef.clef.title == "clef.C") clefVal = ClefValues.C
        if (clef.clef.title == "clef.F") clefVal = ClefValues.F

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
        if (musicalObject is Barline) accidentalByPos.clear()
        if (musicalObject is Clef) setClef(musicalObject)
        if (musicalObject is Accidental) setAccidental(musicalObject)
    }
}
