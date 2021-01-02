package com.github.ojj11

import kotlinx.serialization.Serializable

/** A [Dictionary] of strings to [TagCount]s */
@Serializable
class Dictionary(private val dict: Map<String, TagCount>) {
    /** Get the number of times [word] with [tag] exists in the dictionary */
    fun getCount(word: String?, tag: String?) = dict[word]?.get(tag) ?: 0

    /** Get tags for a given [word] */
    fun getTags(word: String) = dict[word]?.tags ?: emptyArray

    /** Get the most frequent tag for [word], or [fallback] if [word] not found */
    fun getFirstTag(word: String, fallback: String) = dict[word]?.firstTag ?: fallback

    /** Get the frequency of this [word] for all tags */
    fun sum(word: String) = dict[word]?.sum() ?: 0

    /** Does the [word] not exist in this [Dictionary] */
    fun isUnknown(word: String) = !dict.containsKey(word)
}

private val emptyArray: Array<String> = emptyArray()
