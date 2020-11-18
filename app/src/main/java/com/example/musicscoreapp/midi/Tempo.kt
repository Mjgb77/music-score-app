package com.example.musicscoreapp.midi

class Tempo(val tempo: Int) : MidiEvent {
    override fun toByteArray(): ByteArray {
        return byteArrayOf(ByteUtils.FF, 0x51, 0x03) +
                ByteUtils.toBytes(
                    60 * 1_000_000 / tempo,
                    3
                )
    }
}
