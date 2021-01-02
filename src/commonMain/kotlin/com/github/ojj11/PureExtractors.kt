package com.github.ojj11

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * The feature extractors to use for tagging
 */
@Serializable
class PureExtractors(private val extractors: Array<PureExtractor>) {

    /**
     * The extractors that affect the local score
     */
    @Transient
    val local: Array<Pair<Int, PureExtractor>> = filter { isLocal() }

    /**
     * The extractors that affect the total score
     */
    @Transient
    val dynamic: Array<Pair<Int, PureExtractor>> = filter { isDynamic() }

    /**
     * The extractors that affect the local context score
     */
    @Transient
    val localContext: Array<Pair<Int, PureExtractor>> = filter { !isLocal() && !isDynamic() }

    /**
     * The number of features these extractors will generate
     */
    val size = extractors.size

    /**
     * The maximum amount of tokens to the left these extractors will read
     */
    fun leftContext() = extractors.map { it.leftContext() }.maxOrNull() ?: 0

    /**
     * The maximum amount of tokens to the right these extractors will read
     */
    fun rightContext() = extractors.map { it.rightContext() }.maxOrNull() ?: 0

    private fun filter(predicate: PureExtractor.() -> Boolean) =
        extractors.withIndex()
            .filter { it.value.run(predicate) }
            .map { Pair(it.index, it.value) }
            .toTypedArray()

    /**
     * Concatenates extractors together, used to append the rare extractors to the non-rare
     * extractors
     */
    fun combine(other: PureExtractors): PureExtractors {
        return PureExtractors(extractors + other.extractors)
    }
}
