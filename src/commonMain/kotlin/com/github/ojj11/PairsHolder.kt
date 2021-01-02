package com.github.ojj11

/** holds a list of [TaggedWord]s */
class PairsHolder(sentence: Array<String>, var offset: Int = 0) {
    private val arr = Array(sentence.size) { TaggedWord(sentence[it], "NN") }

    operator fun set(pos: Int, tag: String?) {
        arr[pos].tag = tag
    }

    /** gets the word at the given [position] */
    fun getWord(position: Int) = arr.getOrNull(offset + position)?.word ?: "NA"

    /** gets the tag at the given [position] */
    fun getTag(position: Int) = arr.getOrNull(offset + position)?.tag ?: "NN"
}
