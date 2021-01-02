package com.github.ojj11

import kotlinx.serialization.Serializable

/** A set of parameters used in the tagging of text */
@Serializable
data class PureParameters(
    val ySize: Int,
    val extractors: PureExtractors,
    val extractorsRare: PureExtractors,
    val lambda: DoubleArray,
    val dict: Dictionary,
    val tags: TTags,
    val defaultScore: Double,
    val learnClosedClassTags: Boolean,
    val lang: String,
    val rareWordThresh: Int,
    val optimisedFeatures: Map<Feature, Map<String, Int>>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PureParameters

        if (ySize != other.ySize) return false
        if (extractors != other.extractors) return false
        if (extractorsRare != other.extractorsRare) return false
        if (!lambda.contentEquals(other.lambda)) return false
        if (dict != other.dict) return false
        if (tags != other.tags) return false
        if (defaultScore != other.defaultScore) return false
        if (learnClosedClassTags != other.learnClosedClassTags) return false
        if (lang != other.lang) return false
        if (rareWordThresh != other.rareWordThresh) return false
        if (optimisedFeatures != other.optimisedFeatures) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ySize
        result = 31 * result + extractors.hashCode()
        result = 31 * result + extractorsRare.hashCode()
        result = 31 * result + lambda.contentHashCode()
        result = 31 * result + dict.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + defaultScore.hashCode()
        result = 31 * result + learnClosedClassTags.hashCode()
        result = 31 * result + lang.hashCode()
        result = 31 * result + rareWordThresh
        result = 31 * result + optimisedFeatures.hashCode()
        return result
    }
}
