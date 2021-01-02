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
     * sums the log probabilities in [logInputs]
     */
    fun logSum(logInputs: DoubleArray): Double {

        return when (val size = logInputs.size) {
            1 -> logInputs[0]
            2 -> logSumTwoNumbers(logInputs[0], logInputs[1])
            else -> {

                var maxIdx = 0
                var max = logInputs[0]
                for (i in 1 until size) {
                    if (logInputs[i] > max) {
                        maxIdx = i
                        max = logInputs[i]
                    }
                }
                var haveTerms = false
                var intermediate = 0.0
                val cutoff = max - LOG_TOLERANCE
                // we avoid rearranging the array and so test indices each time!
                for (i in 0 until size) {
                    if (i != maxIdx && logInputs[i] > cutoff) {
                        haveTerms = true
                        intermediate += exp(logInputs[i] - max)
                    }
                }

                if (haveTerms) {
                    max + ln(1.0 + intermediate)
                } else {
                    max
                }
            }
        }
    }

    fun logSumTwoNumbers(a: Double, b: Double): Double {
        return if (a < b) {
            if (a > b - LOG_TOLERANCE) {
                b + ln(1.0 + exp(a - b))
            } else {
                b
            }
        } else {
            if (b > a - LOG_TOLERANCE) {
                a + ln(1.0 + exp(b - a))
            } else {
                a
            }
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
