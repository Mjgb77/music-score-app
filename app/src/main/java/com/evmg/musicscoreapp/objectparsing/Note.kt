package com.evmg.musicscoreapp.objectparsing

import com.evmg.musicscoreapp.model.Recognition
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.atan2

class Note(val head: Recognition, val body: Recognition?, val dots: List<Recognition>):
    MusicalObject {
    fun getPitch(context: PitchContext): Int {
        return context.getPitch(head.location)
    }

    fun getDuration():Float {
        var duration = SymbolType.valueByLabel(
            (body ?: head).title.split(".")[1]
        ).factor
        var totalDuration = duration
        for (d in dots.filter { !isVertical(it) }) {
            duration /= 2F
            totalDuration += duration
        }
        return totalDuration
    }

    private fun isVertical(dot: Recognition): Boolean {
        val angle = atan2(
            head.location.centerY() - dot.location.centerY().toDouble(),
            head.location.centerX() - dot.location.centerX().toDouble())
        return min(abs(angle - PI / 2), abs(angle + PI / 2)) < toRad(25.0)
    }

    private fun toRad(deg: Double): Double {
        return deg * PI / 180
    }

    override fun posX() = head.location.left
}
