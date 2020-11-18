package com.example.musicscoreapp.objectparsing

import com.example.musicscoreapp.model.Recognition

class Clef(val clef: Recognition, val accidentals: List<Recognition>):
    MusicalObject {
    override fun posX() = clef.location.left
}
