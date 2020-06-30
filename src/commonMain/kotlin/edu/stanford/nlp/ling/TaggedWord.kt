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
}

const val DIVIDER = "/"