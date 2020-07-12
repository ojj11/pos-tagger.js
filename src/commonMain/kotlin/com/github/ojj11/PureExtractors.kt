package com.github.ojj11

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class PureExtractors(private val extractors: Array<PureExtractor>) {

    @Transient
    val local: Array<Pair<Int, PureExtractor>> = filter { isLocal() }

    @Transient
    val dynamic: Array<Pair<Int, PureExtractor>> = filter { isDynamic() }

    @Transient
    val localContext: Array<Pair<Int, PureExtractor>> = filter { !isLocal() && !isDynamic() }

    val size = extractors.size

    fun leftContext() = extractors.map { it.leftContext() }.max() ?: 0

    fun rightContext() = extractors.map { it.rightContext() }.max() ?: 0

    operator fun get(index: Int) = extractors[index]

    private fun filter(predicate: PureExtractor.() -> Boolean) =
            extractors.withIndex()
                    .filter { it.value.run(predicate) }
                    .map { Pair(it.index, it.value) }
                    .toTypedArray()

    fun combine(other: PureExtractors): PureExtractors {
        return PureExtractors(extractors + other.extractors)
    }
}
