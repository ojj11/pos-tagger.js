package edu.stanford.nlp.tagger.maxent

/**
 *
 * @author Kristina Toutanova
 * @version 1.0
 */
class History internal constructor(val pairs: PairsHolder, private val extractors: PureExtractors) {
    var start = 0 // this is the index of the first word of the sentence = 0
    var end = 0 //this is the index of the last word in the sentence - the dot = 0
    var current = 0 // this is the index of the current word = 0
    fun init(start: Int, end: Int, current: Int) {
        this.start = start
        this.end = end
        this.current = current
    }

    private fun getX(index: Int) = extractors[index].extract(this)

    fun setTag(pos: Int, tag: String?) {
        pairs.setTag(pos + start, tag)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        val x = arrayOfNulls<String>(extractors.size)
        for (i in x.indices) {
            x[i] = getX(i)
        }
        for (aStr in x) {
            sb.append(aStr).append('\t')
        }
        return sb.toString()
    }

    override fun hashCode(): Int {
        val sb = StringBuilder()
        for (i in 0 until (extractors.size)) {
            sb.append(getX(i))
        }
        return sb.toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is History && extractors.equals(this, other)
    }

}