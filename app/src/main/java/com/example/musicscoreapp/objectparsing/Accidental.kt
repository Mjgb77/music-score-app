package com.example.musicscoreapp.objectparsing

import com.example.musicscoreapp.model.Recognition

class Accidental(val body: Recognition):
    MusicalObject {
    override fun posX() = body.location.left
}
