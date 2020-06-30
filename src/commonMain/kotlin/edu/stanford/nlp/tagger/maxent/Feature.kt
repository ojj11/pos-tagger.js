package edu.stanford.nlp.tagger.maxent

import kotlinx.serialization.Serializable

@Serializable
data class Feature(
        private val num: Int,
        private val value: String,
        private val tag: String
) {
    override fun toString() = "$num $value $tag"
}