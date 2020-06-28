package edu.stanford.nlp.tagger.maxent

/** Maintains a set of feature extractors and applies them.
 *
 * @author Kristina Toutanova
 * @version 1.0
 */
@kotlinx.serialization.Serializable
class PureExtractors(private val extractors: Array<PureExtractor>) {

    @kotlinx.serialization.Transient
    var local: MutableMap<Int, PureExtractor>? = null

    @kotlinx.serialization.Transient
    var localContext: MutableMap<Int, PureExtractor>? = null

    @kotlinx.serialization.Transient
    var dynamic: MutableMap<Int, PureExtractor>? = null

    /**
     * Determine type of each feature extractor.
     */
    fun initTypes() {
        local = extractors.withIndex()
                .filter { it.value.isLocal() }
                .map { Pair(it.index, it.value) }
                .toMap()
                .toMutableMap()
        dynamic = extractors.withIndex()
                .filter { it.value.isDynamic() }
                .map { Pair(it.index, it.value) }
                .toMap()
                .toMutableMap()
        localContext = extractors.withIndex()
                .filter { !it.value.isLocal() && !it.value.isDynamic() }
                .map { Pair(it.index, it.value) }
                .toMap()
                .toMutableMap()
    }

    fun equals(h: History, h1: History): Boolean {
        return extractors.all { it.extract(h) == it.extract(h1) }
    }

    /** Find maximum left context of extractors. Used in TagInference to decide windows for dynamic programming.
     * @return The maximum of the left contexts used by all extractors.
     */
    fun leftContext() = extractors.map { it.leftContext() }.max() ?: 0

    /** Find maximum right context of extractors. Used in TagInference to decide windows for dynamic programming.
     * @return The maximum of the right contexts used by all extractors.
     */
    fun rightContext() = extractors.map { it.rightContext() }.max() ?: 0

    val size: Int
        get() = extractors.size

    fun setGlobalHolder(tagger: MaxentTagger) {
        for (extractor in extractors) {
            extractor.setGlobalHolder(tagger)
        }
    }

    operator fun get(index: Int) = extractors[index]

    /**
     * Set the extractors from an array.
     */
    init {
        initTypes()
    }
}