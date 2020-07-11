package com.github.ojj11

import kotlin.math.exp
import kotlin.math.ln

/** Simple maths on arrays */
object ArrayMath {

    /** adds scalar [b] to array [a], updating [a] in place. */
    fun addInPlace(a: DoubleArray, b: Double) {
        for (i in a.indices) {
            a[i] = a[i] + b
        }
    }

    /** adds array [b] to array [a], updating [a] in place. */
    fun pairwiseAddInPlace(to: DoubleArray, from: DoubleArray) {
        if (to.size != from.size) {
            throw RuntimeException()
        }
        for (i in to.indices) {
            to[i] = to[i] + from[i]
        }
    }

    /**
     * sums the log probabilities in [logInputs] including elements [fromIndex] to exclusive
     * [toIndex]
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
        val cutoff = max - LOG_TOLERANCE
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
     * normalise the log probabilities in [a], where [a] cannot be normalised return a uniform
     * dirichlet
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

const val LOG_TOLERANCE = 30.0