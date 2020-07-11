package com.github.ojj11

/** platform independent operations on [Character]s */
object Character {
    const val DASH_PUNCTUATION: Int = 0
    const val CONNECTOR_PUNCTUATION: Int = 1
    const val OTHER_PUNCTUATION: Int = 2
    const val FINAL_QUOTE_PUNCTUATION: Int = 3
    const val INITIAL_QUOTE_PUNCTUATION: Int = 4
    const val END_PUNCTUATION: Int = 5
    const val START_PUNCTUATION: Int = 6
    const val OTHER_SYMBOL: Int = 7
    const val MATH_SYMBOL: Int = 8
    const val CURRENCY_SYMBOL: Int = 9
    const val OTHER_LETTER: Int = 10
    const val OTHER_NUMBER: Int = 11
    const val LETTER_NUMBER: Int = 12

    /** is character [c] a digit */
    fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    /** is character [c] lower case */
    fun isLowerCase(c: Char): Boolean {
        return c in 'a'..'z'
    }

    /** is character [c] upper case */
    fun isUpperCase(c: Char): Boolean {
        return c in 'A'..'Z'
    }

    /** is character [c] title case */
    fun isTitleCase(c: Char): Boolean {
        return Regex("\\p{Titlecase Letter}").matches(c.toString())
    }

    /** get the type of character [c] */
    fun getType(c: Char): Int {
        return when {
            Regex("\\p{Dash Punctuation}").matches(c.toString()) -> DASH_PUNCTUATION
            Regex("\\p{Connector Punctuation}").matches(c.toString()) -> CONNECTOR_PUNCTUATION
            Regex("\\p{Other Punctuation}").matches(c.toString()) -> OTHER_PUNCTUATION
            Regex("\\p{FINAL_QUOTE_PUNCTUATION}").matches(c.toString()) -> FINAL_QUOTE_PUNCTUATION
            Regex("\\p{INITIAL_QUOTE_PUNCTUATION}").matches(c.toString()) -> INITIAL_QUOTE_PUNCTUATION
            Regex("\\p{Close Punctuation}").matches(c.toString()) -> END_PUNCTUATION
            Regex("\\p{Open Punctuation}").matches(c.toString()) -> START_PUNCTUATION
            Regex("\\p{Other Symbol}").matches(c.toString()) -> OTHER_SYMBOL
            Regex("\\p{Math Symbol}").matches(c.toString()) -> MATH_SYMBOL
            Regex("\\p{Currency Symbol}").matches(c.toString()) -> CURRENCY_SYMBOL
            Regex("\\p{Other Letter}").matches(c.toString()) -> OTHER_LETTER
            Regex("\\p{Other Number}").matches(c.toString()) -> OTHER_NUMBER
            Regex("\\p{Letter Number}").matches(c.toString()) -> LETTER_NUMBER
            else -> -1
        }
    }

    /** is character [c] whitespace */
    fun isWhitespace(c: Char): Boolean {
        return c.isWhitespace()
    }

    /** is character [c] a space character */
    fun isSpaceChar(c: Char): Boolean {
        return c == ' '
    }

    /** is character [c] a letter */
    fun isLetter(ch: Char): Boolean {
        return isLowerCase(ch) || isUpperCase(ch)
    }

}