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

    @kotlinx.serialization.Transient
    private var hashCode = 0

    override fun toString() = "$num $value $tag"

    override fun hashCode(): Int {
        if (hashCode == 0) {
           hashCode = arrayOf(num, value, tag).hashCode()
        }
        return hashCode
    }

    override fun equals(o: Any?): Boolean {
        if (o !is FeatureKey) {
            return false
        }
        return num == o.num && tag == o.tag && value == o.value
    }
}