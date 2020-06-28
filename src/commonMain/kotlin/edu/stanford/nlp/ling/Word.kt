package edu.stanford.nlp.ling

/**
 * A `Word` object acts as a Label by containing a String.
 * This class is in essence identical to a `StringLabel`, but
 * it also uses the value to implement the `HasWord` interface.
 *
 * @author Christopher Manning
 * @version 2000/12/20
 */
open class Word(word: String) : StringLabel(word), HasWord {

    override fun word() = value()

    companion object {
        private const val serialVersionUID = -4817252915997034058L
    }
}