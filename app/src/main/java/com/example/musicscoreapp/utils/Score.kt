package com.example.musicscoreapp.utils

import com.example.musicscoreapp.SheetMusic

class Score(
    var title: String,
    var tempo: Int,
    var instrument: Int,
    val sheets: ArrayList<SheetMusic>
)