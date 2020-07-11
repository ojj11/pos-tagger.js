package com.github.ojj11

object Convert {
    fun byteArrToDoubleArr(b: ByteArray): DoubleArray {
        val d = DoubleArray(b.size / 8)
        for (i in 0 until b.size / 8) {
            var l: Long = 0
            for (j in 0..7) {
                l = l or ((b[8 * i + j].toLong() and 0x00000000000000ff) shl 8 * j)
            }
            d[i] = Double.fromBits(l)
        }
        return d
    }
}
