package edu.stanford.nlp.ling

/**
 * Something that implements the `HasTag` interface
 * knows about part-of-speech tags.
 *
 * @author Christopher Manning
 */
interface HasTag {
    fun tag(): String?
}