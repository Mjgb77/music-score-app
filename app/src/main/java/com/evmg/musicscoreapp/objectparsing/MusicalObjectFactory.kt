package com.evmg.musicscoreapp.objectparsing

import com.evmg.musicscoreapp.model.Recognition

object MusicalObjectFactory {
    fun parse(recognition: List<Recognition>): List<MusicalObject> {
        when {
            recognition.any { p -> p.title.startsWith("head") } -> {
                val head = recognition.first { it.title.startsWith("head") }
                val body = recognition.firstOrNull { it.title.startsWith("body") }
                val accidentals = recognition.filter { it.title.startsWith("sign") }.map { Accidental(it) }
                val dots = recognition.filter { it.title.startsWith("dot") }

                return listOf(Note(head, body, dots)) + accidentals
            }
            recognition.any { p -> p.title.startsWith("rest") } -> {
                val body = recognition.first { it.title.startsWith("rest") }
                val dots = recognition.filter { it.title.startsWith("dot") }

                return listOf(Rest(body, dots))
            }
            recognition.any { p -> p.title.startsWith("clef") } -> {
                val clef = recognition.first { it.title.startsWith("clef") }
                val accidentals = recognition.filter { it.title.startsWith("sign") }

                return listOf(Clef(clef, accidentals))
            }
            recognition.any { p -> p.title.startsWith("barline") } -> {
                return listOf(Barline(
                    bar = recognition.first {it.title.startsWith("barline")}
                ))
            }
            recognition.any { p -> p.title.startsWith("sign") } -> {
                return listOf(Accidental(
                    body = recognition.first {it.title.startsWith("sign")}
                ))
            }
            else -> return listOf()
        }
    }
}
