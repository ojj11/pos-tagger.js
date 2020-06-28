package edu.stanford.nlp.maxent

/**
 * This is used to convert an array of double into byte array which makes it possible to keep it more efficiently.
 */
object Convert {
    /** This method allocates a new double[] to return, based on the size of
     * the array b (namely b.length / 8 in size)
     * @param b Array to decode to doubles
     * @return Array of doubles.
     */
    fun byteArrToDoubleArr(b: ByteArray): DoubleArray {
        val length = b.size / 8
        val d = DoubleArray(length)
        for (i in 0 until length) {
            var l: Long = 0
            for (j in 0..7) {
                l = l or ((b[8 * i + j + 0].toLong() and 255) shl 8 * j)
            }
            d[i] = Double.fromBits(l)
        }
        return d
    }

}