package com.evmg.musicscoreapp.objectparsing

import com.evmg.musicscoreapp.model.Recognition

class Rest(val body: Recognition, val dots: List<Recognition>):
    MusicalObject {
    fun getDuration():Float {
        var duration = SymbolType.valueByLabel(
            body.title.split(".")[1]
        ).factor
        var totalDuration = duration
        for (i in dots.indices) {
            duration /= 2F
            totalDuration += duration
        }
        return totalDuration
    }

    override fun posX() = body.location.left
}
