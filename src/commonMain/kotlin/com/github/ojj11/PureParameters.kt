package com.github.ojj11

import kotlinx.serialization.Serializable

/** A set of parameters used in the tagging of text */
@Suppress("ArrayInDataClass")
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
)
