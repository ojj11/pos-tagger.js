package edu.stanford.nlp.io

import kotlinx.serialization.Serializable

@Serializable
data class PureTaggerConfig(
        val defaultScore: Double,
        val learnClosedClassTags: Boolean,
        val closedClassTags: Array<String>,
        val openClassTags: Array<String>,
        val lang: String,
        val rareWordThresh: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PureTaggerConfig

        if (defaultScore != other.defaultScore) return false
        if (learnClosedClassTags != other.learnClosedClassTags) return false
        if (!closedClassTags.contentEquals(other.closedClassTags)) return false
        if (!openClassTags.contentEquals(other.openClassTags)) return false
        if (lang != other.lang) return false
        if (rareWordThresh != other.rareWordThresh) return false

        return true
    }

    override fun hashCode(): Int {
        var result = defaultScore.hashCode()
        result = 31 * result + learnClosedClassTags.hashCode()
        result = 31 * result + closedClassTags.contentHashCode()
        result = 31 * result + openClassTags.contentHashCode()
        result = 31 * result + lang.hashCode()
        result = 31 * result + rareWordThresh
        return result
    }
}