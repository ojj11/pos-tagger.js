package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.util.HashIndex
import edu.stanford.nlp.util.Index

private fun defaultClosedTags(language: String): MutableSet<String> {
    val closed = mutableSetOf<String>()
    when {
        language.equals("english", ignoreCase = true) -> {
            closed.add(".")
            closed.add(",")
            closed.add("``")
            closed.add("''")
            closed.add(":")
            closed.add("$")
            closed.add("EX")
            closed.add("(")
            closed.add(")")
            closed.add("#")
            closed.add("MD")
            closed.add("CC")
            closed.add("DT")
            closed.add("LS")
            closed.add("PDT")
            closed.add("POS")
            closed.add("PRP")
            closed.add("PRP$")
            closed.add("RP")
            closed.add("TO")
            closed.add("EOS")
            closed.add("UH")
            closed.add("WDT")
            closed.add("WP")
            closed.add("WP$")
            closed.add("WRB")
            closed.add("-LRB-")
            closed.add("-RRB-")
            //  closed.add("IN");
        }
        language.equals("polish", ignoreCase = true) -> {
            closed.add(".")
            closed.add(",")
            closed.add("``")
            closed.add("''")
            closed.add(":")
            closed.add("$")
            closed.add("(")
            closed.add(")")
            closed.add("#")
            closed.add("POS")
            closed.add("EOS")
            closed.add("ppron12")
            closed.add("ppron3")
            closed.add("siebie")
            closed.add("qub")
            closed.add("conj")
        }
        language.equals("chinese", ignoreCase = true) -> {
            /* chinese treebank 5 tags */
            closed.add("AS")
            closed.add("BA")
            closed.add("CC")
            closed.add("CS")
            closed.add("DEC")
            closed.add("DEG")
            closed.add("DER")
            closed.add("DEV")
            closed.add("DT")
            closed.add("ETC")
            closed.add("IJ")
            closed.add("LB")
            closed.add("LC")
            closed.add("P")
            closed.add("PN")
            closed.add("PU")
            closed.add("SB")
            closed.add("SP")
            closed.add("VC")
            closed.add("VE")
        }
        language.equals("arabic", ignoreCase = true) -> {
            closed.add("PUNC")
            closed.add("CONJ")
            // maybe more should still be added ... cdm jun 2006
        }
        language.equals("german", ignoreCase = true) -> {
            closed.add("$,")
            closed.add("$.")
            closed.add("$")
        }
        language.equals("medpost", ignoreCase = true) -> {
            closed.add(".")
            closed.add(",")
            closed.add("``")
            closed.add("''")
            closed.add(":")
            closed.add("$")
            closed.add("EX")
            closed.add("(")
            closed.add(")")
            closed.add("VM")
            closed.add("CC")
            closed.add("DD")
            closed.add("DB")
            closed.add("GE")
            closed.add("PND")
            closed.add("PNG")
            closed.add("TO")
            closed.add("EOS")
            closed.add("-LRB-")
            closed.add("-RRB-")
        }
        language.equals("", ignoreCase = true) -> {
        }
        else -> {
            throw RuntimeException("unknown language: $language")
        }
    }
    return closed
}

/**
 * This class holds the POS tags, assigns them unique ids, and knows which tags
 * are open versus closed class.
 *
 *
 * Title:        StanfordMaxEnt
 *
 *
 * Description:  A Maximum Entropy Toolkit
 *
 *
 * Company:      Stanford University
 *
 *
 *
 * @author Kristina Toutanova
 * @version 1.0
 */
class TTags constructor(
        language: String = "",
        val closedClassTags: MutableSet<String> = defaultClosedTags(language),
        var openClassTags: MutableSet<String>? = null
) {
    private var index: Index<String> = HashIndex()
    private var isEnglish = language.equals("english", ignoreCase = true)

    /** Caches values returned by deterministicallyExpandTags.
     * This assumes that each word comes always with the same tag (wasn't proven false so far).
     */
    private val deterministicExpansionMemoizer: MutableMap<String?, Array<String?>> = HashMap()

    /**
     * Returns a list of all open class tags
     * @return set of open tags
     */
    fun getOpenTags(): Set<String?>? {
        if (openClassTags == null) {
            openClassTags = mutableSetOf(*index.filter { it !in closedClassTags }.toTypedArray())
        }
        return openClassTags
    }

    fun getTag(i: Int) = index[i]

    fun merge(tags: List<String>, closed: List<String>) {
        closedClassTags.addAll(closed)
        tags.forEach { index.add(it) }
    }

    fun getIndex(tag: String?): Int {
        return index.indexOf(tag)
    }

    val size: Int
        get() = index.getSizeOverride()

    /**
     * Deterministically adds other possible tags for words given observed tags.
     * For instance, for English with the Penn POS tag, a word with the VB
     * tag would also be expected to have the VBP tag.
     * (CDM May 2007: This was putting repeated values into the set of possible
     * tags, which was bad.  Now it doesn't, but the resulting code is a funny
     * mixture of trying to micro-optimize, and just using equals() inside a
     * List linear scan....
     *
     * @param tags Known possible tags for the word
     * @param word The word (currently not a used parameter)
     * @return A superset of tags
     */
    fun deterministicallyExpandTags(tags: Array<String?>, word: String?): Array<String?> {
        synchronized(deterministicExpansionMemoizer) {
            val arrayOfStrings = deterministicExpansionMemoizer[word]
            if (arrayOfStrings != null) return arrayOfStrings
        }
        return if (isEnglish) {
            val tl = ArrayList<String?>(tags.size + 2)
            val yVBD = getIndex("VBD")
            val yVBN = getIndex("VBN")
            val yVBP = getIndex("VBP")
            val yVB = getIndex("VB")
            if (yVBD < 0 || yVBN < 0 || yVBP < 0 || yVB < 0) {
                return tags
            }
            for (tag in tags) {
                val y = getIndex(tag)
                addIfAbsent(tl, tag)
                when (y) {
                    yVBD -> {
                        addIfAbsent(tl, "VBN")
                    }
                    yVBN -> {
                        addIfAbsent(tl, "VBD")
                    }
                    yVB -> {
                        addIfAbsent(tl, "VBP")
                    }
                    yVBP -> {
                        addIfAbsent(tl, "VB")
                    }
                }
            } // end for i
            val newtags = tl.toTypedArray()
            synchronized(deterministicExpansionMemoizer) { deterministicExpansionMemoizer.put(word, newtags) }
            newtags
        } else {
            // no tag expansion for other languages currently
            tags
        }
    }

    companion object {
        const val CLOSED_TAG_THRESHOLD = 40

        // TODO: this should be in some kind of list util
        private fun addIfAbsent(list: MutableList<String?>, item: String?) {
            if (!list.contains(item)) {
                list.add(item)
            }
        }
    }
}