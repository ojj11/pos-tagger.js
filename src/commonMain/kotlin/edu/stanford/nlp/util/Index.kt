package edu.stanford.nlp.util

/**
 * Minimalist interface for implementations of Index.
 *
 * This interface should allow Index and OAIndex to be used interchangeably
 * in certain contexts.
 *
 * Originally extracted from util.Index on 3/13/2007
 *
 * @author Daniel Cer
 *
 * @param <E>
</E> */
interface Index<E> : Iterable<E> {
    /**
     * Returns the number of indexed objects.
     * @return the number of indexed objects.
     */
    fun getSizeOverride(): Int

    /**
     * Gets the object whose index is the integer argument.
     * @param i the integer index to be queried for the corresponding argument
     * @return the object whose index is the integer argument.
     */
    operator fun get(i: Int): E

    /**
     * Returns the integer index of the Object in the Index or -1 if the Object is not already in the Index.
     * @param o the Object whose index is desired.
     * @return the index of the Object argument.  Returns -1 if the object is not in the index.
     */
    fun indexOf(o: E): Int

    // mg2009. Methods below were temporarily added when IndexInterface was renamed
    // to Index. These methods are currently (2009-03-09) needed in order to have core classes
    // of JavaNLP (Dataset, LinearClassifier, etc.) use Index instead of HashIndex.
    // Possible javanlp task: delete some of these methods.
    // Subset of the Collection interface:
    fun add(e: E): Boolean
}