package edu.stanford.nlp.ling

/**
 * Something that implements the `HasOffset` interface
 * bears a offset reference to the original text
 *
 * @author Richard Eckart (Technische Universitat Darmstadt)
 */
interface HasOffset {
    /**
     * Return the beginning character offset of the label (or -1 if none).
     *
     * @return the beginning position for the label
     */
    fun beginPosition(): Int

    /**
     * Return the ending character offset of the label (or -1 if none).
     *
     * @return the end position for the label
     */
    fun endPosition(): Int
}