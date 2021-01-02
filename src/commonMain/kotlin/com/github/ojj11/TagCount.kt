package com.github.ojj11

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/** a set of tags and associated scores */
@Serializable
class TagCount(private var map: Map<String, Int>) {

    @Transient
    val tags = map.keys.toTypedArray()

    /** the highest scoring tag */
    val firstTag: String?
        get() = map.maxByOrNull { it.value }?.key

    /** the total scores for normalisation */
    fun sum() = map.values.sum()

    /** get the score of the given [tag] */
    operator fun get(tag: String?) = map[tag] ?: 0

    override fun toString() = map.toString()
}
