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
     * @return the number of total occurrences of the word .
     */
    fun sum() = map.values.sum()

    operator fun get(tag: String?) = map[tag] ?: 0

    /**
     * @return an array of the tags the word has had.
     */
    val tags: Array<String?> = map.keys.toTypedArray()

    /**
     * @return the most frequent tag.
     */
    val firstTag: String?
        get() {
            var maxTag: String? = null
            var max = 0
            for (tag in map.keys) {
                val count = map[tag]!!
                if (count > max) {
                    maxTag = tag
                    max = count
                }
            }
            return maxTag
        }

    override fun toString() = map.toString()

    companion object {
        const val NULL_SYMBOL = "<<NULL>>"
    }
}