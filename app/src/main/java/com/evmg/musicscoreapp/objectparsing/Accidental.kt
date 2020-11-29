package com.evmg.musicscoreapp.objectparsing

import com.evmg.musicscoreapp.model.Recognition

class Accidental(val body: Recognition):
    MusicalObject {
    override fun posX() = body.location.left
}
