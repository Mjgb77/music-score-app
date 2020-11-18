package com.example.musicscoreapp.midi

import java.io.ByteArrayOutputStream

class MidiWriter() {
    private val stream = ByteArrayOutputStream()
    fun writeHeader() : MidiWriter {
        stream.write(ByteUtils.toBytes("MThd"))
        stream.write(
            ByteUtils.toBytes(
                6,
                4
            )
        ) // chunklen
        stream.write(
            ByteUtils.toBytes(
                1,
                2
            )
        ) // format
        stream.write(
            ByteUtils.toBytes(
                2,
                2
            )
        ) // ntracks
        stream.write(
            ByteUtils.toBytes(
                960,
                2
            )
        ) // tickdiv
        return this
    }

    fun writeTrack(track: Track) : MidiWriter {
        stream.write(ByteUtils.toBytes("MTrk"))
        val bytes = track.toByteArray()
        stream.write(
            ByteUtils.toBytes(
                bytes.size,
                4
            )
        )
        stream.write(bytes)
        return this
    }
    fun toByteArray() : ByteArray {
        return stream.toByteArray()
    }
}
