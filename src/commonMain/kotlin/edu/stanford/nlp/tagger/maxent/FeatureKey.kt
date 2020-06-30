package edu.stanford.nlp.tagger.maxent

/**
 * Stores a triple of an extractor ID, a feature value (derived from history)
 * and a y (tag) value.  Used to compute a feature number in the loglinear
 * model.
 *
 * @author Kristina Toutanova, with minor changes by Daniel Cer
 * @version 1.0
 */
@kotlinx.serialization.Serializable
class FeatureKey(
        private val num: Int,
        private val value: String,
        private val tag: String?
) {
    override fun toString() = "$num $value $tag"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FeatureKey

        if (num != other.num) return false
        if (value != other.value) return false
        if (tag != other.tag) return false

        return true
    }

    override fun hashCode(): Int {
        var result = num
        result = 31 * result + value.hashCode()
        result = 31 * result + (tag?.hashCode() ?: 0)
        return result
    }
}