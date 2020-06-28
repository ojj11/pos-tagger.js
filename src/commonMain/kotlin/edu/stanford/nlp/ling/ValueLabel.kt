package edu.stanford.nlp.ling

/**
 * A `ValueLabel` object acts as a Label with linguistic
 * attributes.  This is an abstract class, which doesn't actually store
 * or return anything.  It returns `null` to any requests. However,
 * it does
 * stipulate that equals() and compareTo() are defined solely with respect to
 * value(); this should not be changed by subclasses.
 * Other fields of a ValueLabel subclass should be regarded
 * as secondary facets (it is almost impossible to override equals in
 * a useful way while observing the contract for equality defined for Object,
 * in particular, that equality must by symmetric).
 * This class is designed to be extended.
 *
 * @author Christopher Manning
 */
abstract class ValueLabel protected constructor() : Label, Comparable<ValueLabel> {
    /**
     * Return the value of the label (or null if none).
     * The default value returned by an `ValueLabel` is
     * always `null`
     *
     * @return the value for the label
     */
    override fun value(): String? = null

    /**
     * Return a string representation of the label.  This will just
     * be the `value()` if it is non-`null`,
     * and the empty string otherwise.
     *
     * @return The string representation
     */
    override fun toString(): String {
        val `val` = value()
        return `val` ?: ""
    }

    /**
     * Equality for `ValueLabel`s is defined in the first instance
     * as equality of their `String` `value()`.
     * Now rewritten to correctly enforce the contract of equals in Object.
     * Equality for a `ValueLabel` is determined simply by String
     * equality of its `value()`.  Subclasses should not redefine
     * this to include other aspects of the `ValueLabel`, or the
     * contract for `equals()` is broken.
     *
     * @param obj the object against which equality is to be checked
     * @return true if `this` and `obj` are equal
     */
    override fun equals(obj: Any?): Boolean {
        val `val` = value()
        return obj is ValueLabel && if (`val` == null) (obj as Label).value() == null else `val` == (obj as Label).value()
    }

    /**
     * Return the hashCode of the String value providing there is one.
     * Otherwise, returns an arbitrary constant for the case of
     * `null`.
     */
    override fun hashCode() = value()?.hashCode() ?: 3

    /**
     * Orders by `value()`'s lexicographic ordering.
     *
     * @param valueLabel object to compare to
     * @return result (positive if this is greater than obj)
     */
    override fun compareTo(valueLabel: ValueLabel): Int {
        return value()!!.compareTo(valueLabel.value()!!)
    }

    companion object {
        private const val serialVersionUID = -1413303679077285530L
    }
}