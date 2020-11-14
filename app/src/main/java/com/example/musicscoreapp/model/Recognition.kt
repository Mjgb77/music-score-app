package com.example.musicscoreapp.model

import android.graphics.RectF

class Recognition(
    val id: String,
    val title: String,
    val confidence: Float,
    val location: RectF,
    val detectedClass: Int
)
