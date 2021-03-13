package com.evmg.musicscoreapp.midi

class NoteOn(val note: Byte, val velocity: Byte = 0x64) :
        MidiEvent {
    override fun toByteArray(): ByteArray {
        return byteArrayOf(0x90.toByte(), note, velocity)
    }
}
