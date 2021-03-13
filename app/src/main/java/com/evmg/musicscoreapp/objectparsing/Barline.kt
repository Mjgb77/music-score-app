package com.evmg.musicscoreapp.objectparsing

import com.evmg.musicscoreapp.model.Recognition

class Barline(val bar: Recognition):
    MusicalObject {
    override fun posX() = bar.location.left
}
