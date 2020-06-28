package edu.stanford.nlp.ling

/**
 * Sentence holds a couple utility methods for lists.
 * Those include a method that nicely prints a list and methods that
 * construct lists of words from lists of strings.
 *
 * @author Dan Klein
 * @author Christopher Manning (generified)
 * @author John Bauer
 * @version 2010
 */
object Sentence {
    /**
     * Returns the sentence as a string with a space between words.
     * Designed to work robustly, even if the elements stored in the
     * 'Sentence' are not of type Label.
     *
     * This one uses the default separators for any word type that uses
     * separators, such as TaggedWord.
     *
     * @param justValue If `true` and the elements are of type
     * `Label`, return just the
     * `value()` of the `Label` of each word;
     * otherwise,
     * call the `toString()` method on each item.
     * @return The sentence in String form
     */
    fun <T> listToString(list: List<T>, justValue: Boolean): String {
        return listToString(list, justValue, null)
    }

    /**
     * As already described, but if separator is not null, then objects
     * such as TaggedWord
     *
     * @param separator The string used to separate Word and Tag
     * in TaggedWord, etc
     */
    private fun <T> listToString(list: List<T>, justValue: Boolean,
                         separator: String?): String {
        val s = StringBuilder()
        val wordIterator = list.iterator()
        while (wordIterator.hasNext()) {
            val o = wordIterator.next()
            if (justValue && o is Label) {
                s.append((o as Label).value())
                // TODO: this should be before the justValue case
            } else if (separator != null && o is TaggedWord) {
                s.append((o as TaggedWord).toString(separator))
            } else if (separator != null && o is WordTag) {
                s.append((o as WordTag).toString(separator))
            } else {
                s.append(o.toString())
            }
            if (wordIterator.hasNext()) {
                s.append(' ')
            }
        }
        return s.toString()
    }
}