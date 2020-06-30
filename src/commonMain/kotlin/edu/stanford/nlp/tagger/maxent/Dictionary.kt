package edu.stanford.nlp.tagger.maxent

import kotlinx.serialization.Serializable

@Serializable
class Dictionary(private val dict: Map<String, TagCount>) {
    fun getCount(word: String?, tag: String?) = dict[word]?.get(tag) ?: 0

    fun getTags(word: String) = dict[word]?.tags ?: emptyArray()

    fun getFirstTag(word: String, fallback: String) = dict[word]?.firstTag ?: fallback

    fun sum(word: String) = dict[word]?.sum() ?: 0

    fun isUnknown(word: String) = !dict.containsKey(word)
}