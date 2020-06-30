package edu.stanford.nlp.ling

/** A [word] with an optional part-of-speech [tag] */
data class TaggedWord(val word: String, var tag: String?) {

    override fun toString() = toString(DIVIDER)

    private fun toString(divider: String) = """${word}$divider$tag"""
}

private const val DIVIDER = "/"