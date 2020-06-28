package edu.stanford.nlp.tagger.maxent

/** Maintains a map from words to tags and their counts.
 *
 * @author Kristina Toutanova
 * @version 1.0
 */
@kotlinx.serialization.Serializable
class Dictionary(private val dict: Map<String?, TagCount>) {

    fun getCount(word: String?, tag: String?) = dict[word]?.get(tag) ?: 0

    fun getTags(word: String?) = get(word)?.tags

    private operator fun get(word: String?) = dict[word]

    fun getFirstTag(word: String?) = dict[word]?.firstTag

    fun sum(word: String?) = dict[word]?.sum() ?: 0

    fun isUnknown(word: String?) = !dict.containsKey(word)
}