package edu.stanford.nlp.math

import kotlin.math.exp
import kotlin.math.ln

object ArrayMath {

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

    fun logSum(logInputs: DoubleArray, fromIndex: Int = 0): Double {
        val size = logInputs.size
        if (fromIndex >= 0 && size < logInputs.size && fromIndex >= size) {
            return Double.NEGATIVE_INFINITY
        }
        val subList = logInputs.drop(fromIndex)
        val (maxIdx, max) = subList.withIndex().maxBy { it.value }
                ?: throw IllegalArgumentException("range has zero elements")

        val cutoff = max - LOG_TOLERANCE

        val values = subList.filterIndexed { index, value ->
            index != maxIdx && value > cutoff
        }

        return if (values.isEmpty()) {
            max
        } else {
            max + ln(1.0 + values.fold(0.0) { acc, e -> acc + exp(e - max) })
        }
    }

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