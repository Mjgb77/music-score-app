package com.evmg.musicscoreapp.midi

class ProgramChange(val instrument: Byte) :
        MidiEvent {
    override fun toByteArray(): ByteArray {
        return byteArrayOf(0xC0.toByte(), instrument)
    }
}
