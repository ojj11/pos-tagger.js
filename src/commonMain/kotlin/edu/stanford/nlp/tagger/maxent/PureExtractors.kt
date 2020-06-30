package edu.stanford.nlp.tagger.maxent

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class PureExtractors(private val extractors: Array<PureExtractor>) {

    @Transient
    val local: Map<Int, PureExtractor> = filter { it.value.isLocal() }

    @Transient
    val dynamic: Map<Int, PureExtractor> = filter { it.value.isDynamic() }

    @Transient
    val localContext: Map<Int, PureExtractor> = filter {
        !it.value.isLocal() && !it.value.isDynamic()
    }

    val size = extractors.size

    fun leftContext() = extractors.map { it.leftContext() }.max() ?: 0

    fun rightContext() = extractors.map { it.rightContext() }.max() ?: 0

    fun setGlobalHolder(tagger: MaxentTagger) {
        for (extractor in extractors) {
            extractor.setGlobalHolder(tagger)
        }
    }

    operator fun get(index: Int) = extractors[index]

    private fun filter(predicate: (IndexedValue<PureExtractor>) -> Boolean) =
            extractors.withIndex()
                    .filter(predicate)
                    .map { Pair(it.index, it.value) }
                    .toMap()
                    .toMutableMap()
}
