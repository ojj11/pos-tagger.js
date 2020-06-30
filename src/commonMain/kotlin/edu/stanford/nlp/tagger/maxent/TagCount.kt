package edu.stanford.nlp.tagger.maxent

/**
 * This class was created to store the possible tags of a word along with how many times
 * the word appeared with each tag.
 *
 * @author Kristina Toutanova
 * @version 1.0
 */
@kotlinx.serialization.Serializable
class TagCount(private var map: Map<String?, Int>) {

    /**
     * @return an array of the tags the word has had.
     */
    @kotlinx.serialization.Transient
    val tags: Array<String?> = map.keys.toTypedArray()

    /**
     * @return the number of total occurrences of the word .
     */
    fun sum() = map.values.sum()

    operator fun get(tag: String?) = map[tag] ?: 0

    /**
     * @return the most frequent tag.
     */
    val firstTag: String?
        get() = map.maxBy { it.value }?.key

    override fun toString() = map.toString()

    companion object {
        const val NULL_SYMBOL = "<<NULL>>"
    }
}