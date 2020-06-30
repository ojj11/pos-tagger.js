package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.util.BiList

class TTags constructor(
        language: String = "",
        val closedClassTags: MutableSet<String> = defaultClosedTags(language),
        openClassTags: Set<String>? = null
) {
    private var index = BiList<String>()
    private var isEnglish = language.equals("english", ignoreCase = true)
    val openClassTags by lazy {
        openClassTags ?: index.filter { it !in closedClassTags }.toSet()
    }

    fun getTag(i: Int) = index[i]

    fun merge(tags: List<String>, closed: List<String>) {
        closedClassTags.addAll(closed)
        tags.forEach { index.add(it) }
    }

    fun getIndex(tag: String): Int {
        return index.indexOf(tag)
    }

    private val tagCache = mutableMapOf<String?, Array<String>>()

    fun deterministicallyExpandTags(tags: Array<String>, word: String?): Array<String> {
        return if (isEnglish) {
            var tags = tagCache[word] ?: tags
            if ("VBD" in tags && "VBN" !in tags) {
                tags += "VBN"
            }
            if ("VBN" in tags && "VBD" !in tags) {
                tags += "VBD"
            }
            if ("VB" in tags && "VBP" !in tags) {
                tags += "VBP"
            }
            if ("VBN" in tags && "VB" !in tags) {
                tags += "VB"
            }
            tagCache[word] = tags
            tags
        } else {
            // no tag expansion for other languages currently
            tags
        }
    }

}

private fun defaultClosedTags(language: String): MutableSet<String> {
    val closed = hashSetOf<String>()
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