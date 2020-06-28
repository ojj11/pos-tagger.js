package edu.stanford.nlp.ling

/**
 * Something that implements the `HasWord` interface
 * knows about words.
 *
 * @author Christopher Manning
 */
interface HasWord {
    /**
     * Return the word value of the label (or null if none).
     *
     * @return String the word value for the label
     */
    fun word(): String?
}