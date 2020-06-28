package edu.stanford.nlp.ling

/**
 * A `TaggedWord` object contains a word and its tag.
 * The `value()` of a TaggedWord is the Word.  The tag
 * is secondary.
 *
 * @author Christopher Manning
 */
class TaggedWord(word: String?, private val tag: String?) : Word(word!!), HasTag {
    override fun toString() = toString(DIVIDER)

    override fun tag() = tag

    fun toString(divider: String) = word() + divider + tag

    companion object {
        private const val DIVIDER = "/"
        private const val serialVersionUID = -7252006452127051085L
    }

}