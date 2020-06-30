package edu.stanford.nlp.io

import edu.stanford.nlp.tagger.maxent.*
import kotlinx.serialization.Serializable

@Serializable
data class PureModel(
        val ySize: Int,
        val extractors: PureExtractors,
        val extractorsRare: PureExtractors,
        val features: Map<Feature, Int>,
        val lambda: DoubleArray,
        val dict: Dictionary,
        val tags: List<String>,
        val closedClassTags: List<String>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PureModel

        if (ySize != other.ySize) return false
        if (extractors != other.extractors) return false
        if (extractorsRare != other.extractorsRare) return false
        if (features != other.features) return false
        if (!lambda.contentEquals(other.lambda)) return false
        if (dict != other.dict) return false
        if (tags != other.tags) return false
        if (closedClassTags != other.closedClassTags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ySize
        result = 31 * result + extractors.hashCode()
        result = 31 * result + extractorsRare.hashCode()
        result = 31 * result + features.hashCode()
        result = 31 * result + lambda.contentHashCode()
        result = 31 * result + dict.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + closedClassTags.hashCode()
        return result
    }
}