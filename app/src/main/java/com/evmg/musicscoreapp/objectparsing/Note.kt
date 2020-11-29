package com.evmg.musicscoreapp.objectparsing

import com.evmg.musicscoreapp.model.Recognition

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
        for (i in dots.indices) {
            duration /= 2F
            totalDuration += duration
        }
        return totalDuration
    }

    override fun posX() = head.location.left
}
