package edu.stanford.nlp.ling

/**
 * A WordTag corresponds to a tagged (e.g., for part of speech) word
 * and is implemented with String-valued word and tag.  It implements
 * the Label interface; the `value()` method for that
 * interface corresponds to the word of the WordTag.
 *
 *
 * The equality relation for WordTag is defined as identity of both
 * word and tag.  Note that this is different from
 * `TaggedWord`, for which equality derives from
 * `ValueLabel` and requires only identity of value.
 * @author Roger Levy
 */
class WordTag(word: String?, tag: String?) : Label, HasWord, HasTag, Comparable<WordTag> {
    private var word: String? = null
    private var tag: String? = null

    /**
     * Return a String representation of just the "main" value of this label.
     *
     * @return the "value" of the label
     */
    override fun value() = word

    override fun word() = value()

    /**
     * Set the value for the label (if one is stored).
     *
     * @param value - the value for the label
     */
    private fun setValue(value: String?) {
        word = value
    }

    override fun tag() = tag

    private fun setWord(word: String?) {
        setValue(word)
    }

    fun setTag(tag: String?) {
        this.tag = tag
    }

    /**
     * Return a String representation of the label.  For a multipart label,
     * this will return all parts.  The `toString()` method
     * causes a label to spill its guts.  It should always return an
     * empty string rather than `null` if there is no value.
     *
     * @return a text representation of the full label contents
     */
    override fun toString(): String = toString(DIVIDER)

    fun toString(divider: String): String {
        val tag = tag()
        return if (tag == null) {
            word()!!
        } else {
            word() + divider + tag
        }
    }

    /** A WordTag is equal only to another WordTag with the same word and tag values.
     */
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is WordTag) return false
        return if (tag != o.tag) false else word == o.word
    }

    override fun hashCode(): Int {
        var result: Int = if (word != null) word.hashCode() else 0
        result = 29 * result + if (tag != null) tag.hashCode() else 0
        return result
    }

    /**
     * Orders first by word, then by tag.
     *
     * @param wordTag object to compare to
     * @return result (positive if `this` is greater than
     * `obj`, 0 if equal, negative otherwise)
     */
    override fun compareTo(wordTag: WordTag): Int {
        val first = if (word != null) word()!!.compareTo(wordTag.word()!!) else 0
        return if (first != 0) first else {
            if (tag() == null) {
                return if (wordTag.tag() == null) 0 else -1
            }
            tag()!!.compareTo(wordTag.tag()!!)
        }
    }

    /**
     * Create a new `WordTag`.
     */
    init {
        setWord(word)
        setTag(tag)
    }
}