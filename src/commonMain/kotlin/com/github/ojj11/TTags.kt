package com.github.ojj11

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class TTags(
        val tags: Array<String>,
        val openClassTags: Array<String>
) {
    @Transient
    private var index = tags.withIndex().map { it.value to it.index }.toMap()

    fun getTag(i: Int) = tags[i]

    fun getIndex(tag: String): Int = index[tag] ?: -1
}
