package edu.stanford.nlp.ling

/**
 * A `StringLabel` object acts as a Label by containing a
 * single String, which it sets or returns in response to requests.
 * The hashCode() and compareTo() methods for this class assume that this
 * string value is non-null.  equals() is correctly implemented
 *
 * @author Christopher Manning
 * @version 2000/12/20
 */
open class StringLabel(private val str: String) : ValueLabel(), HasOffset {

    /**
     * Start position of the word in the original input string
     */
    private var beginPosition = -1

    /**
     * End position of the word in the original input string
     */
    private var endPosition = -1

    /**
     * Return the word value of the label (or null if none).
     *
     * @return String the word value for the label
     */
    override fun value() = str

    override fun toString() = str

    override fun beginPosition() = beginPosition

    override fun endPosition() = endPosition

    private fun setBeginPosition(beginPosition: Int) {
        this.beginPosition = beginPosition
    }

    private fun setEndPosition(endPosition: Int) {
        this.endPosition = endPosition
    }

    companion object {
        private const val serialVersionUID = -4153619273767524247L
    }
}