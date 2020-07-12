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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        other as Feature

        if (extractorIndex != other.extractorIndex) return false
        if (extractedValue != other.extractedValue) return false

        return true
    }

    override fun hashCode() = 31 * extractorIndex + extractedValue.hashCode()
}