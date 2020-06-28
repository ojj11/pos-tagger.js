package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.ling.WordTag

/** A simple class that maintains a list of WordTag pairs which are interned
 * as they are added.  This stores a tagged corpus.
 *
 * @author Kristina Toutanova
 * @version 1.0
 */
class PairsHolder(sentence: List<String>) {
    private val arr = sentence.map { WordTag(it, "NA") }

    fun setTag(pos: Int, tag: String?) {
        arr[pos].setTag(tag)
    }

    fun getWord(position: Int) = arr[position].word()

    fun getWord(h: History, position: Int) = arr.getOrNull(h.current + position)?.word() ?: "NA"

    fun getTag(h: History, position: Int) = arr.getOrNull(h.current + position)?.tag() ?: "NA"
}