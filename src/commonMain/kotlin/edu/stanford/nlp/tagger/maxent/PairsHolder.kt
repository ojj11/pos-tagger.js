package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.ling.TaggedWord

class PairsHolder(sentence: List<String>, var offset: Int = 0) {
    private val arr = sentence.map { TaggedWord(it, "NA") }

    operator fun set(pos: Int, tag: String?) {
        arr[pos].tag = tag
    }

    fun getWord(position: Int) = arr.getOrNull(offset + position)?.word ?: "NA"

    fun getTag(position: Int) = arr.getOrNull(offset + position)?.tag ?: "NA"
}