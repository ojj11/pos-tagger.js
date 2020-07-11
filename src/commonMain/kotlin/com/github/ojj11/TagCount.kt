package com.github.ojj11

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class TagCount(private var map: Map<String, Int>) {

    @Transient
    val tags = map.keys.toTypedArray()

    val firstTag: String?
        get() = map.maxBy { it.value }?.key

    fun sum() = map.values.sum()

    operator fun get(tag: String?) = map[tag] ?: 0

    override fun toString() = map.toString()

}