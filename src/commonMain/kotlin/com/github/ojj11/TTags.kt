package com.github.ojj11

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/** a data structure for quickly mapping tags between string and numerical index form */
@Serializable
class TTags(
    val tags: Array<String>,
    val openClassTags: Array<String>
) {
    @Transient
    private var index = tags.withIndex().map { it.value to it.index }.toMap()

    /** get the tag at the given index */
    fun getTag(i: Int) = tags[i]

    /** get the index for the given tag */
    fun getIndex(tag: String): Int = index[tag] ?: -1
}
