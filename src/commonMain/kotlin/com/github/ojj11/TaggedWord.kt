package com.github.ojj11

/** A immutable [word] with an optional mutable part-of-speech [tag] */
data class TaggedWord(
    val word: String,
    var tag: String?
) {

    override fun toString() = toString(DIVIDER)

    private fun toString(divider: String) =
        """${word}$divider$tag"""
}

private const val DIVIDER = "/"
