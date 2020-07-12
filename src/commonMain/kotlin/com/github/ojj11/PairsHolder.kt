package com.github.ojj11

class PairsHolder(sentence: Array<String>, var offset: Int = 0) {
    private val arr = Array(sentence.size) { TaggedWord(sentence[it], "NN") }

    operator fun set(pos: Int, tag: String?) {
        arr[pos].tag = tag
    }

    fun getWord(position: Int) = arr.getOrNull(offset + position)?.word ?: "NA"

    fun getTag(position: Int) = arr.getOrNull(offset + position)?.tag ?: "NN"
}