package com.evmg.musicscoreapp.midi

class NoteOff(val note: Byte, val velocity: Byte = 0x64) :
        MidiEvent {
    override fun toByteArray(): ByteArray {
        return byteArrayOf(0x80.toByte(), note, velocity)
    }
}
