package com.example.musicscoreapp.objectparsing

import com.example.musicscoreapp.model.Recognition

class Barline(val bar: Recognition):
    MusicalObject {
    override fun posX() = bar.location.left
}
