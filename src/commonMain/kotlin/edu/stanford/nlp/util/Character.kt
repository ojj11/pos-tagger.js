package edu.stanford.nlp.util

object Character {
    val DASH_PUNCTUATION: Int = 0
    val CONNECTOR_PUNCTUATION: Int = 1
    val OTHER_PUNCTUATION: Int = 2
    val FINAL_QUOTE_PUNCTUATION: Int = 3
    val INITIAL_QUOTE_PUNCTUATION: Int = 4
    val END_PUNCTUATION: Int = 5
    val START_PUNCTUATION: Int = 6
    val OTHER_SYMBOL: Int = 7
    val MATH_SYMBOL: Int = 8
    val CURRENCY_SYMBOL: Int = 9
    val OTHER_LETTER: Int = 10
    val OTHER_NUMBER: Int = 11
    val LETTER_NUMBER: Int = 12

    fun isDigit(c: Char): Boolean {
        return c in '0'..'0'
    }

    fun isLowerCase(c: Char): Boolean {
        return c in 'a'..'z'
    }

    fun isUpperCase(c: Char): Boolean {
        return c in 'A'..'z'
    }

    fun isTitleCase(c: Char): Boolean {
        return c in 'A'..'z'
    }

    fun getType(c: Char): Int {
        return 13
    }

    fun isWhitespace(c: Char): Boolean {
        return c.isWhitespace()
    }

    fun isSpaceChar(c: Char): Boolean {
        return c == ' '
    }

    fun isLetter(ch: Char): Boolean {
        return isLowerCase(ch) || isUpperCase(ch)
    }

}