package edu.stanford.nlp.ling

/**
 * Something that implements the `Label` interface can act as a
 * constituent, node, or word label with linguistic attributes.
 * A `Label` is required to have a "primary" `String`
 * `value()` (although this may be null).  This is referred to as
 * its `value`.
 *
 *
 * Implementations of Label split into two groups with
 * respect to equality. Classes that extend ValueLabel define equality
 * solely in terms of String equality of its value (secondary facets may be
 * present but are ignored for purposes of equality), and have equals and
 * compareTo defined across all subclasses of ValueLabel. This behavior
 * should not be changed. Other classes that implement Label define equality only
 * with their own type and require all fields of the type to be equal.
 *
 *
 * A subclass that extends another Label class *should* override
 * the definition of `labelFactory()`, since the contract for
 * this method is that it should return a factory for labels of the
 * exact same object type.
 *
 * @author Christopher Manning
 */
interface Label {
    /**
     * Return a String representation of just the "main" value of this label.
     *
     * @return the "value" of the label
     */
    fun value(): String?

    /**
     * Return a String representation of the label.  For a multipart label,
     * this will return all parts.  The `toString()` method
     * causes a label to spill its guts.  It should always return an
     * empty string rather than `null` if there is no value.
     *
     * @return a text representation of the full label contents
     */
    override fun toString(): String
}