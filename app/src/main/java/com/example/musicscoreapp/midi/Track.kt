package com.example.musicscoreapp.midi

import java.io.ByteArrayOutputStream

class Track {
    val stream = ByteArrayOutputStream()
    fun addEvent(delta: Int, event: MidiEvent) : Track {
        stream.write(
            ByteUtils.deltaTime(
                delta
            )
        )
        stream.write(event.toByteArray())
        return this
    }
    fun toByteArray() : ByteArray {
        return stream.toByteArray() + byteArrayOf(0x00,
            ByteUtils.FF, 0x2F, 0x00)
    }
}
