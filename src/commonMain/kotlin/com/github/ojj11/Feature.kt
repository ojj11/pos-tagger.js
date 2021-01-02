package com.github.ojj11

import kotlinx.serialization.Serializable

/**
 * Sparse [Feature] consisting of the index of the [Extractor] as [extractorIndex], and
 * the weight as [extractedValue]
 */
@Serializable
data class Feature(
    val extractorIndex: Int,
    val extractedValue: String
)
