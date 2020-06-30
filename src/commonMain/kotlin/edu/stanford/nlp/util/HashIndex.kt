package edu.stanford.nlp.util

/**
 * An Index is a collection that maps between an Object vocabulary and a
 * contiguous non-negative integer index series beginning (inclusively) at 0.
 * It supports constant-time lookup in
 * both directions (via `get(int)` and `indexOf(E)`.
 * The `indexOf(E)` method compares objects by
 * `equals`, as other Collections.
 *
 *
 * The typical usage would be:
 *
 * `Index index = new Index(collection);`
 *
 *  followed by
 *
 * `int i = index.indexOf(object);`
 *
 *  or
 *
 * `Object o = index.get(i);`
 *
 * The source contains a concrete example of use as the main method.
 *
 * An Index can be locked or unlocked: a locked index cannot have new
 * items added to it.
 *
 * @author [Dan Klein](mailto:klein@cs.stanford.edu)
 * @version 1.0
 * @see AbstractCollection
 *
 * @since 1.0
 * @author [Eric Yeh](mailto:yeh1@stanford.edu) (added write to/load from buffer)
 */
class HashIndex<E> : AbstractCollection<E>(), Index<E>, RandomAccess {
    // these variables are also used in IntArrayIndex
    private val objects = ArrayList<E>()
    private val indexes = HashMap<E, Int?>()

    /**
     * Gets the object whose index is the integer argument.
     * @param i the integer index to be queried for the corresponding argument
     * @return the object whose index is the integer argument.
     */
    override fun get(i: Int): E {
        return objects[i]
    }

    /**
     * Takes an Object and returns the integer index of the Object,
     * perhaps adding it to the index first.
     * Returns -1 if the Object is not in the Index.
     *
     *
     * *Notes:* The method indexOf(x, true) is the direct replacement for
     * the number(x) method in the old Numberer class.  This method now uses a
     * Semaphore object to make the index safe for concurrent multithreaded
     * usage. (CDM: Is this better than using a syncronized block?)
     *
     * @param o the Object whose index is desired.
     * @return The index of the Object argument.  Returns -1 if the object is not in the index.
     */
    override fun indexOf(o: E): Int {
        return indexes[o] ?: return -1
    }
    // TODO: delete this because we can leach off of Abstract Collection
    /**
     * Adds an object to the Index. If it was already in the Index,
     * then nothing is done.  If it is not in the Index, then it is
     * added iff the Index hasn't been locked.
     *
     */
    override fun add(o: E): Boolean {
        var index = indexes[o]
        if (index == null) {
            index = objects.size
            objects.add(o)
            indexes[o] = index
        }
        return true
    }

    /**
     * Checks whether an Object already has an index in the Index
     * @param element the object to be queried.
     * @return true iff there is an index for the queried object.
     */
    override operator fun contains(element: E): Boolean {
        return indexes.containsKey(element)
    }

    /** Returns a readable version of the Index contents
     *
     * @return A String showing the full index contents
     */
    override fun toString(): String {
        return toString(Int.MAX_VALUE)
    }

    /** Returns a readable version of at least part of the Index contents.
     *
     * @param n Show the first *n* items in the Index
     * @return A String rshowing some of the index contents
     */
    fun toString(n: Int): String {
        var n = n
        val buff = StringBuilder("[")
        val sz = objects.size
        if (n > sz) {
            n = sz
        }
        var i = 0
        while (i < n) {
            val e = objects[i]
            buff.append(i).append("=").append(e)
            if (i < sz - 1) buff.append(",")
            i++
        }
        if (i < sz) buff.append("...")
        buff.append("]")
        return buff.toString()
    }

    /**
     * Returns an iterator over the elements of the collection.
     * @return An iterator over the objects indexed
     */
    override fun iterator(): MutableIterator<E> {
        return objects.iterator()
    }

    override fun getSizeOverride() = objects.size
    override val size: Int
        get() = objects.size
}