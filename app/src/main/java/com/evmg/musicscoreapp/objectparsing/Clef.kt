package com.evmg.musicscoreapp.objectparsing

import com.evmg.musicscoreapp.model.Recognition

class Clef(val clef: Recognition, val accidentals: List<Recognition>):
    MusicalObject {
    override fun posX() = clef.location.left
}
