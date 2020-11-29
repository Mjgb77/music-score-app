package com.evmg.musicscoreapp.midi

import java.nio.charset.Charset

class ByteUtils {
    companion object {
        const val FF = 255.toByte()
        fun toBytes(pnum: Int, width: Int): ByteArray {
            val bytes = ArrayList<Byte>()
            var num = pnum
            for (i in 0 until width) {
                bytes.add((num % 256).toByte())
                num /= 256
            }
            return bytes.reversed().toByteArray()
        }

        fun toBytes(str: String): ByteArray {
            return str.toByteArray(Charset.forName("ASCII"))
        }

        fun deltaTime(time: Int): ByteArray {
            var vtime = time
            val bytes = ArrayList<Byte>()
            do {
                bytes.add((vtime % 128).toByte())
                vtime /= 128
            } while (vtime > 0)
            for (i in bytes.indices.drop(1)) {
                bytes[i] = (bytes[i] - 128).toByte()
            }
            return bytes.reversed().toByteArray()
        }
    }
}
