package edu.stanford.nlp.math

import kotlin.math.exp
import kotlin.math.ln

/**
 * Class ArrayMath
 *
 * @author Teg Grenager
 */
object ArrayMath {

    /**
     * Increases the values in this array by b. Does it in place.
     *
     * @param a The array
     * @param b The amount by which to increase each item
     */
    fun addInPlace(a: DoubleArray, b: Double) {
        for (i in a.indices) {
            a[i] = a[i] + b
        }
    }

    fun pairwiseAddInPlace(to: DoubleArray, from: DoubleArray) {
        if (to.size != from.size) {
            throw RuntimeException()
        }
        for (i in to.indices) {
            to[i] = to[i] + from[i]
        }
    }

    /**
     * Returns the log of the sum of an array of numbers, which are
     * themselves input in log form.  This is all natural logarithms.
     * Reasonable care is taken to do this as efficiently as possible
     * (under the assumption that the numbers might differ greatly in
     * magnitude), with high accuracy, and without numerical overflow.
     *
     * @param logInputs An array of numbers [log(x1), ..., log(xn)]
     * @return log(x1 + ... + xn)
     */
    fun logSum(logInputs: DoubleArray, fromIndex: Int = 0, toIndex: Int = logInputs.size): Double {
        if (fromIndex >= 0 && toIndex < logInputs.size && fromIndex >= toIndex) return Double.NEGATIVE_INFINITY
        var maxIdx = fromIndex
        var max = logInputs[fromIndex]
        for (i in fromIndex + 1 until toIndex) {
            if (logInputs[i] > max) {
                maxIdx = i
                max = logInputs[i]
            }
        }
        var haveTerms = false
        var intermediate = 0.0
        val cutoff = max - SloppyMath.LOGTOLERANCE
        // we avoid rearranging the array and so test indices each time!
        for (i in fromIndex until toIndex) {
            if (i != maxIdx && logInputs[i] > cutoff) {
                haveTerms = true
                intermediate += exp(logInputs[i] - max)
            }
        }
        return if (haveTerms) {
            max + ln(1.0 + intermediate)
        } else {
            max
        }
    }

    /**
     * Makes the values in this array sum to 1.0. Does it in place.
     * If the total is 0.0, throws a RuntimeException.
     * If the total is Double.NEGATIVE_INFINITY, then it replaces the
     * array with a normalized uniform distribution. CDM: This last bit is
     * weird!  Do we really want that?
     */
    fun logNormalize(a: DoubleArray) {
        val logTotal = logSum(a)
        if (logTotal == Double.NEGATIVE_INFINITY) {
            // to avoid NaN values
            val v = -ln(a.size.toDouble())
            a.fill(v)
            return
        }
        addInPlace(a, -logTotal)
    }
}