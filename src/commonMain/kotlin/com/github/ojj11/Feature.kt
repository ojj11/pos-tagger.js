package com.github.ojj11

import kotlinx.serialization.Serializable

/**
 * Sparse [Feature] consisting of the index of the [Extractor] as [extractorIndex], the
 * [extractedValue] and the [tag]
 */
@Serializable
data class Feature(
    val extractorIndex: Int,
    val extractedValue: String
)
