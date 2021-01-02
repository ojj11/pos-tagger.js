@file:Suppress("CanSealedSubClassBeObject")

package com.github.ojj11

import kotlinx.serialization.Serializable

@Serializable
sealed class PureExtractor {

    open fun getPosition() = 0

    open fun leftContext() = 0

    open fun rightContext() = 0

    abstract fun extract(pH: PairsHolder, dict: Dictionary): String

    abstract fun isDynamic(): Boolean

    open fun isLocal() = !isDynamic() && getPosition() == 0

    companion object {
        const val zeroSt = "0"
    }
}

val cWord = PureExtractorBasic(0, false)

@Serializable
class PureExtractorBasic(
    private var privatePosition: Int = Int.MAX_VALUE,
    private var privateIsDynamic: Boolean = false
) : PureExtractor() {

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return if (isDynamic()) pH.getTag(getPosition()) else pH.getWord(getPosition())
    }

    override fun leftContext(): Int {
        if (privateIsDynamic) {
            if (getPosition() < 0) {
                return -getPosition()
            }
        }
        return 0
    }

    override fun rightContext(): Int {
        if (privateIsDynamic) {
            if (getPosition() > 0) {
                return getPosition()
            }
        }
        return 0
    }

    override fun getPosition() = privatePosition

    override fun isDynamic() = privateIsDynamic
}

@Serializable
class PureExtractorWordLowerCase(private var privatePosition: Int) : PureExtractor() {

    override fun getPosition() = privatePosition

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return pH.getWord(privatePosition).toLowerCase()
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

@Serializable
class PureExtractorTwoWords(private var privatePosition: Int) : PureExtractor() {
    override fun getPosition() = privatePosition

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return """${pH.getWord(privatePosition)}!${pH.getWord(privatePosition + 1)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = false
}

@Serializable
class PureExtractorCWordNextWord : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return pH.getWord(0) + '!' + pH.getWord(1)
    }

    override fun isLocal() = false

    override fun isDynamic() = false
}

@Serializable
class PureExtractorCWordPrevWord : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return pH.getWord(-1) + '!' + pH.getWord(0)
    }

    override fun isLocal() = false

    override fun isDynamic() = false
}

@Serializable
class PureExtractorPrevTwoTags : PureExtractor() {
    override fun leftContext(): Int {
        return 2
    }

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return pH.getTag(-1) + '!' + pH.getTag(-2)
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

@Serializable
class PureExtractorPrevThreeTags : PureExtractor() {
    override fun leftContext(): Int {
        return 3
    }

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return """${pH.getTag(-1)}!${pH.getTag(-2)}!${pH.getTag(-3)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

@Serializable
class PureExtractorNextTwoTags : PureExtractor() {
    override fun rightContext(): Int {
        return 2
    }

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return """${pH.getTag(1)}!${pH.getTag(2)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

@Serializable
class PureExtractorPrevTagWord : PureExtractor() {
    override fun leftContext(): Int {
        return 1
    }

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return """${pH.getTag(-1)}!${pH.getWord(0)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

@Serializable
class PureExtractorPrevTagNextTag : PureExtractor() {
    override fun leftContext(): Int {
        return 1
    }

    override fun rightContext(): Int {
        return 1
    }

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return """${pH.getTag(-1)}!${pH.getTag(1)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

@Serializable
class PureExtractorNextTagWord : PureExtractor() {
    override fun rightContext(): Int {
        return 1
    }

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return """${pH.getTag(1)}!${pH.getWord(0)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

@Serializable
class PureCompanyNameDetector(private val companyNameEnds: MutableSet<String?>) : PureExtractor() {

    private fun companyNameEnd(s: String?): Boolean {
        return companyNameEnds.contains(s)
    }

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val s = pH.getWord(0)
        if (!startsUpperCase(s)) {
            return "0"
        }
        for (i in 0..3) {
            val s1 = pH.getWord(i)
            if (companyNameEnd(s1)) {
                return "1"
            }
        }
        return "0"
    }

    override fun isLocal() = false

    override fun isDynamic() = false

    init {
        companyNameEnds.add("Company")
        companyNameEnds.add("COMPANY")
        companyNameEnds.add("Co.")
        companyNameEnds.add("Co")
        companyNameEnds.add("Cos.")
        companyNameEnds.add("CO.")
        companyNameEnds.add("COS.")
        companyNameEnds.add("Corporation")
        companyNameEnds.add("CORPORATION")
        companyNameEnds.add("Corp.")
        companyNameEnds.add("Corp")
        companyNameEnds.add("CORP.")
        companyNameEnds.add("Incorporated")
        companyNameEnds.add("INCORPORATED")
        companyNameEnds.add("Inc.")
        companyNameEnds.add("Inc")
        companyNameEnds.add("INC.")
        companyNameEnds.add("Association")
        companyNameEnds.add("ASSOCIATION")
        companyNameEnds.add("Assn")
        companyNameEnds.add("ASSN")
        companyNameEnds.add("Limited")
        companyNameEnds.add("LIMITED")
        companyNameEnds.add("Ltd.")
        companyNameEnds.add("LTD.")
        companyNameEnds.add("L.P.")
    }
}

@Serializable
class PureExtractorUCase : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val s = pH.getWord(0)
        return if (containsUpperCase(s)) {
            "1"
        } else "0"
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

@Serializable
class PureExtractorLetterDigitDash : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val s = pH.getWord(0)
        return if (containsLetter(s) && containsDash(s) && containsNumber(s)) {
            "1"
        } else "0"
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

@Serializable
class PureExtractorUpperDigitDash : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val s = pH.getWord(0)
        return if (containsUpperCase(s) && containsDash(s) && containsNumber(s)) {
            "1"
        } else "0"
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

@Serializable
class PureExtractorCapDistLC : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val word = pH.getWord(0)
        val ret: String
        if (!startsUpperCase(word)) {
            return "0"
        }
        ret = if (allUpperCase(word)) {
            "all:"
        } else {
            "start"
        }

        var current = -1
        var distance = 1
        while (true) {
            val prevWord = pH.getWord(current)
            if (startsLowerCase(prevWord)) {
                return ret + distance
            }
            if (prevWord == naTag || prevWord == "``") {
                return ret + "infinity"
            }
            current--
            distance++
        }
    }

    override fun isDynamic() = false

    override fun isLocal() = false
}

@Serializable
class PureExtractorCapLCSeen(val tag: String?) : PureExtractor() {

    @kotlinx.serialization.Transient
    private val cutoff = 1

    @kotlinx.serialization.Transient
    private val cCapDist: PureExtractor = PureExtractorCapDistLC()

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val res = cCapDist.extract(pH, dict)
        if (res == "0") {
            return res
        }

        val word = cWord.extract(pH, dict)
        return if (dict.getCount(word, tag) > cutoff) {
            res + tag
        } else {
            "0"
        }
    }

    override fun isLocal() = false

    override fun isDynamic() = false
}

@Serializable
class PureExtractorMidSentenceCap : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val prevTag = pH.getTag(-1)
        if (prevTag == naTag) {
            return "0"
        }
        val s = pH.getWord(0)
        return if (containsUpperCase(s)) {
            "1"
        } else "0"
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

@Serializable
class PureExtractorStartSentenceCap : PureExtractor() {

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val prevTag = pH.getTag(-1)
        if (prevTag != naTag) {
            return zeroSt
        }
        val s = pH.getWord(0)
        if (startsUpperCase(s)) {
            val s1 = s.toLowerCase()
            return dict.getFirstTag(s1, zeroSt)
        }
        return zeroSt
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

@Serializable
class PureExtractorMidSentenceCapC : PureExtractor() {

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val prevTag = pH.getTag(-1)
        if (prevTag == naTag) {
            return zeroSt
        }
        val s = pH.getWord(0)
        if (startsUpperCase(s)) {
            val s1 = s.toLowerCase()
            return dict.getFirstTag(s1, zeroSt)
        }
        return zeroSt
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

@Serializable
class PureExtractorAllCap : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val s = pH.getWord(0)
        return if (noneLowerCase(s)) {
            "1"
        } else "0"
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

@Serializable
class PureExtractorAllCapitalized : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val s = pH.getWord(0)
        return if (allUpperCase(s)) {
            "1"
        } else "0"
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

@Serializable
class PureExtractorCNumber : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val s = pH.getWord(0)
        return if (s.contains(Regex("\\d"))) {
            "1"
        } else {
            "0"
        }
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

@Serializable
class PureExtractorDash : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val s = pH.getWord(0)
        return if (containsDash(s)) {
            "1"
        } else {
            "0"
        }
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

@Serializable
class PureExtractorCWordSuff(private val num: Int) : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val word = pH.getWord(0)
        return if (word.length < num) {
            "######"
        } else {
            word.substring(word.length - num)
        }
    }

    override fun toString(): String {
        return super.toString() + " size " + num
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

@Serializable
class PureExtractorCWordPref(private val num: Int) : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val word = pH.getWord(0)
        return if (word.length < num) {
            "######"
        } else {
            word.substring(0, num)
        }
    }

    override fun toString(): String {
        return super.toString() + " size " + num
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

@Serializable
class PureExtractorsConjunction(private val extractor1: PureExtractor, private val extractor2: PureExtractor) : PureExtractor() {

    private val privateIsLocal: Boolean = extractor1.isLocal() && extractor2.isLocal()
    private val privateIsDynamic: Boolean = extractor1.isDynamic() || extractor2.isDynamic()

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val ex1 = extractor1.extract(pH, dict)
        if (ex1 == zeroSt) {
            return zeroSt
        }
        val ex2 = extractor2.extract(pH, dict)
        return if (ex2 == zeroSt) {
            zeroSt
        } else {
            "$ex1:$ex2"
        }
    }

    override fun isLocal() = privateIsLocal

    override fun isDynamic() = privateIsDynamic
}

@Serializable
class PureExtractorWordShapeClassifier(private val position: Int, private val wordShaper: Int) : PureExtractor() {

    override fun getPosition() = position

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val s = if (isDynamic()) pH.getTag(getPosition()) else pH.getWord(getPosition())
        return WordShapeClassifier.wordShape(s, wordShaper)
    }

    override fun isLocal() = position == 0

    override fun isDynamic() = false
}

@Serializable
class PureExtractorWordShapeConjunction(private val left: Int, private val right: Int, private val wordShaper: Int) : PureExtractor() {
    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val sb = StringBuilder()
        for (j in left..right) {
            val s = pH.getWord(j)
            sb.append(WordShapeClassifier.wordShape(s, wordShaper))
            if (j < right) {
                sb.append('|')
            }
        }
        return sb.toString()
    }

    override fun isLocal() = false

    override fun isDynamic() = false
}

@Serializable
open class PureDictionaryExtractor : PureExtractor() {

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        return if (isDynamic()) pH.getTag(getPosition()) else pH.getWord(getPosition())
    }

    override fun isDynamic() = false
}

@Serializable
class PureExtractorVerbalVBNZero(private val bound: Int) : PureDictionaryExtractor() {

    override fun extract(pH: PairsHolder, dict: Dictionary): String {
        val cword = pH.getWord(0)
        val allCount = dict.sum(cword)
        val vBNCount = dict.getCount(cword, vbnTag)
        val vBDCount = dict.getCount(cword, vbdTag)

        if (allCount == 0 && !(cword.endsWith(edSuff) || cword.endsWith(enSuff))) {
            return zeroSt
        }
        if (allCount > 0 && vBNCount + vBDCount <= allCount / 100) {
            return zeroSt
        }
        var lastverb: String? = naWord
        var index = -1
        while (index >= -bound) {
            val word2 = pH.getWord(index)
            if ("NA" == word2) {
                break
            }
            if (stopper.matches(word2)) {
                break
            }
            if (vbnWord.matches(word2)) {
                lastverb = word2
                break
            }
            index--
            index--
        }
        if (lastverb != naWord) {
            return oneSt
        }
        return zeroSt
    }

    override fun toString(): String {
        return "ExtractorVerbalVBNZero(bound=$bound)"
    }

    companion object {
        private const val vbnTag = "VBN"
        private const val vbdTag = "VBD"
        private const val edSuff = "ed"
        private const val enSuff = "en"
        private const val oneSt = "1"
        private const val naWord = "NA"
        private val stopper = Regex("(?i:and|or|but|,|;|-|--)")
        private val vbnWord = Regex("(?i:have|has|having|had|is|am|are|was|were|be|being|been|'ve|'s|s|'d|'re|'m|gotten|got|gets|get|getting)")
    }
}

private const val naTag = "NA"

fun startsUpperCase(s: String?) = s?.first()?.let { Character.isUpperCase(it) } ?: false

fun startsLowerCase(s: String?) = s?.first()?.let { Character.isLowerCase(it) } ?: false

fun containsDash(s: String?) = s?.any { it == '-' } ?: false

fun containsNumber(s: String?) = s?.any { Character.isDigit(it) } ?: false

fun containsLetter(s: String?) = s?.any { Character.isLetter(it) } ?: false

fun containsUpperCase(s: String?) = s?.any { Character.isUpperCase(it) } ?: false

fun allUpperCase(s: String?) = s?.all { Character.isUpperCase(it) } ?: false

fun noneLowerCase(s: String?) = s?.all { !Character.isLowerCase(it) } ?: false
