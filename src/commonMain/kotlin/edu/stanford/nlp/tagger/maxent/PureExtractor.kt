@file:Suppress("CanSealedSubClassBeObject")

package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.process.WordShapeClassifier
import edu.stanford.nlp.util.Character
import kotlinx.serialization.Serializable

@Serializable
sealed class PureExtractor {

    open fun getPosition(): Int {
        return Int.MAX_VALUE
    }

    open fun setGlobalHolder(tagger: MaxentTagger) {}

    open fun leftContext() = 0

    open fun rightContext() = 0

    abstract fun extract(pH: PairsHolder): String

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

    override fun extract(pH: PairsHolder): String {
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

/**
 * The word in lower-cased version.
 */
@Serializable
class PureExtractorWordLowerCase(private var privatePosition: Int) : PureExtractor() {

    override fun getPosition() = privatePosition

    override fun extract(pH: PairsHolder): String {
        return pH.getWord(privatePosition).toLowerCase()
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

/**
 * This extractor extracts two consecutive words in conjunction,
 * namely leftPosition and leftPosition+1.
 */
@Serializable
class PureExtractorTwoWords(private var privatePosition: Int) : PureExtractor() {
    override fun getPosition() = privatePosition

    override fun extract(pH: PairsHolder): String {
        return """${pH.getWord(privatePosition)}!${pH.getWord(privatePosition + 1)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = false
}

/**
 * This extractor extracts the current and the next word in conjunction.
 */
@Serializable
class PureExtractorCWordNextWord : PureExtractor() {
    override fun extract(pH: PairsHolder): String {
        return pH.getWord(0) + '!' + pH.getWord(1)
    }

    override fun isLocal() = false

    override fun isDynamic() = false
}

/**
 * This extractor extracts the current and the previous word in conjunction.
 */
@Serializable
class PureExtractorCWordPrevWord : PureExtractor() {
    override fun extract(pH: PairsHolder): String {
        return pH.getWord(-1) + '!' + pH.getWord(0)
    }

    override fun isLocal() = false

    override fun isDynamic() = false
}

/**
 * This extractor extracts the previous two tags.
 */
@Serializable
class PureExtractorPrevTwoTags : PureExtractor() {
    override fun leftContext(): Int {
        return 2
    }

    override fun extract(pH: PairsHolder): String {
        return pH.getTag(-1) + '!' + pH.getTag(-2)
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

/**
 * This extractor extracts the previous three tags.
 */
@Serializable
class PureExtractorPrevThreeTags : PureExtractor() {
    override fun leftContext(): Int {
        return 3
    }

    override fun extract(pH: PairsHolder): String {
        return """${pH.getTag(-1)}!${pH.getTag(-2)}!${pH.getTag(-3)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

/**
 * This extractor extracts the next two tags.
 */
@Serializable
class PureExtractorNextTwoTags : PureExtractor() {
    override fun rightContext(): Int {
        return 2
    }

    override fun extract(pH: PairsHolder): String {
        return """${pH.getTag(1)}!${pH.getTag(2)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

/**
 * This extractor extracts the previous tag and the current word in conjunction.
 */
@Serializable
class PureExtractorPrevTagWord : PureExtractor() {
    override fun leftContext(): Int {
        return 1
    }

    override fun extract(pH: PairsHolder): String {
        return """${pH.getTag(-1)}!${pH.getWord(0)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

/**
 * This extractor extracts the previous tag , next tag in conjunction.
 */
@Serializable
class PureExtractorPrevTagNextTag : PureExtractor() {
    override fun leftContext(): Int {
        return 1
    }

    override fun rightContext(): Int {
        return 1
    }

    override fun extract(pH: PairsHolder): String {
        return """${pH.getTag(-1)}!${pH.getTag(1)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

/**
 * This extractor extracts the next tag and the current word in conjunction.
 */
@Serializable
class PureExtractorNextTagWord : PureExtractor() {
    override fun rightContext(): Int {
        return 1
    }

    override fun extract(pH: PairsHolder): String {
        return """${pH.getTag(1)}!${pH.getWord(0)}"""
    }

    override fun isLocal() = false

    override fun isDynamic() = true
}

/** English-specific crude company name NER.  */
@Serializable
class PureCompanyNameDetector(private val companyNameEnds: MutableSet<String?>) : PureExtractor() {

    private fun companyNameEnd(s: String?): Boolean {
        return companyNameEnds.contains(s)
    }

    override fun extract(pH: PairsHolder): String {
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
        companyNameEnds.add("Co") // at end of sentence in PTB
        companyNameEnds.add("Cos.")
        companyNameEnds.add("CO.")
        companyNameEnds.add("COS.")
        companyNameEnds.add("Corporation")
        companyNameEnds.add("CORPORATION")
        companyNameEnds.add("Corp.")
        companyNameEnds.add("Corp") // at end of sentence in PTB
        companyNameEnds.add("CORP.")
        companyNameEnds.add("Incorporated")
        companyNameEnds.add("INCORPORATED")
        companyNameEnds.add("Inc.")
        companyNameEnds.add("Inc") // at end of sentence in PTB
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
        // companyNameEnds.add("PLC"); // Other thing added at same time.
    }
} // end class CompanyNameDetector

@Serializable
class PureExtractorUCase : PureExtractor() {
    override fun extract(pH: PairsHolder): String {
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
    override fun extract(pH: PairsHolder): String {
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
    override fun extract(pH: PairsHolder): String {
        val s = pH.getWord(0)
        return if (containsUpperCase(s) && containsDash(s) && containsNumber(s)) {
            "1"
        } else "0"
    }

    override fun isLocal() = true

    override fun isDynamic() = false
}

/**
 * creates features which are true if the current word is all caps
 * and the distance to the first lowercase word to the left is dist
 * the distance is 1 for adjacent, 2 for one across, 3 for ... and so on.
 * inifinity if no capitalized word (we hit the start of sentence or '')
 */
@Serializable
class PureExtractorCapDistLC : PureExtractor() {
    override fun extract(pH: PairsHolder): String {
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

        //now find the distance
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

/**
 * This feature applies when the word is capitalized
 * and the previous lower case is infinity
 * and the lower cased version of it has occured 2 or more times with tag t
 * false if the word was not seen.
 * create features only for tags that are the same as the tag t
 */
@Serializable
class PureExtractorCapLCSeen(val tag: String?) : PureExtractor() {

    @kotlinx.serialization.Transient
    private val cutoff = 1

    @kotlinx.serialization.Transient
    private val cCapDist: PureExtractor = PureExtractorCapDistLC()

    @kotlinx.serialization.Transient
    private lateinit var dict: Dictionary

    override fun setGlobalHolder(tagger: MaxentTagger) {
        dict = tagger.dict
    }

    override fun extract(pH: PairsHolder): String {
        val res = cCapDist.extract(pH)
        if (res == "0") {
            return res
        }
        //otherwise it is capitalized
        val word = cWord.extract(pH)
        return if (dict.getCount(word, tag) > cutoff) {
            res + tag
        } else {
            "0"
        }
    }

    override fun isLocal() = false

    override fun isDynamic() = false
}

/**
 * "1" if not first word of sentence and _some_ letter is uppercase
 */
@Serializable
class PureExtractorMidSentenceCap : PureExtractor() {
    override fun extract(pH: PairsHolder): String {
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

/**
 * "0" if not 1st word of sentence or not upper case, or lowercased version
 * not in dictionary.  Else first tag of word lowercased.
 */
@Serializable
class PureExtractorStartSentenceCap : PureExtractor() {

    @kotlinx.serialization.Transient
    private lateinit var dict: Dictionary

    override fun setGlobalHolder(tagger: MaxentTagger) {
        dict = tagger.dict
    }

    override fun extract(pH: PairsHolder): String {
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

/**
 * "0" if first word of sentence or not first letter uppercase or if
 * lowercase version isn't in dictionary.  Otherwise first tag of lowercase
 * equivalent.
 */
@Serializable
class PureExtractorMidSentenceCapC : PureExtractor() {

    @kotlinx.serialization.Transient
    private lateinit var dict: Dictionary

    override fun setGlobalHolder(tagger: MaxentTagger) {
        dict = tagger.dict
    }

    override fun extract(pH: PairsHolder): String {
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
} // TODO: the next time we have to rebuild the tagger files anyway, we

// should change this class's name to something like
// "ExtractorNoLowercase" to distinguish it from
// ExtractorAllCapitalized
@Serializable
class PureExtractorAllCap : PureExtractor() {
    override fun extract(pH: PairsHolder): String {
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
    override fun extract(pH: PairsHolder): String {
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
    override fun extract(pH: PairsHolder): String {
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
    override fun extract(pH: PairsHolder): String {
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
    override fun extract(pH: PairsHolder): String {
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
    override fun extract(pH: PairsHolder): String {
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
} // end class ExtractorCWordPref

@Serializable
class PureExtractorsConjunction(private val extractor1: PureExtractor, private val extractor2: PureExtractor) : PureExtractor() {

    private val privateIsLocal: Boolean = extractor1.isLocal() && extractor2.isLocal()
    private val privateIsDynamic: Boolean = extractor1.isDynamic() || extractor2.isDynamic()

    override fun setGlobalHolder(tagger: MaxentTagger) {
        extractor1.setGlobalHolder(tagger)
        extractor2.setGlobalHolder(tagger)
    }

    override fun extract(pH: PairsHolder): String {
        val ex1 = extractor1.extract(pH)
        if (ex1 == zeroSt) {
            return zeroSt
        }
        val ex2 = extractor2.extract(pH)
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

    override fun extract(pH: PairsHolder): String {
        val s = if (isDynamic()) pH.getTag(getPosition()) else pH.getWord(getPosition())
        return WordShapeClassifier.wordShape(s, wordShaper)
    }

    override fun isLocal() = position == 0

    override fun isDynamic() = false
}

/**
 * This extractor extracts a conjunction of word shapes.
 */
@Serializable
class PureExtractorWordShapeConjunction(private val left: Int, private val right: Int, private val wordShaper: Int) : PureExtractor() {
    override fun extract(pH: PairsHolder): String {
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

/**
 * This class is the same as a regular Extractor, but keeps a pointer
 * to the tagger's dictionary as well.
 *
 * Obviously that means this kind of extractor is not reusable across
 * multiple taggers (see comments Extractor.java), so no extractor of
 * this type should be declared static.
 */
@Serializable
open class PureDictionaryExtractor : PureExtractor() {
    /**
     * A pointer to the creating / owning tagger's dictionary.
     */
    @kotlinx.serialization.Transient
    protected lateinit var dict: Dictionary

    /**
     * Any subclass of this extractor that overrides setGlobalHolder
     * should call this class's setGlobalHolder as well...
     */
    override fun setGlobalHolder(tagger: MaxentTagger) {
        super.setGlobalHolder(tagger)
        dict = tagger.dict
    }

    override fun extract(pH: PairsHolder): String {
        return if (isDynamic()) pH.getTag(getPosition()) else pH.getWord(getPosition())
    }

    override fun isDynamic() = false
}

/**
 * Look for verbs selecting a VBN verb.
 * This is now a zeroeth order observed data only feature.
 * But reminiscent of what was done in Toutanova and Manning 2000.
 * It doesn't seem to help tagging performance any more.
 *
 * @author Christopher Manning
 */
@Serializable
class PureExtractorVerbalVBNZero(private val bound: Int) : PureDictionaryExtractor() {

    override fun extract(pH: PairsHolder): String {
        val cword = pH.getWord(0)
        val allCount = dict.sum(cword)
        val vBNCount = dict.getCount(cword, vbnTag)
        val vBDCount = dict.getCount(cword, vbdTag)

        // Conditions for deciding inapplicable
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
        private val vbnWord = Regex("(?i:have|has|having|had|is|am|are|was|were|be|being|been|'ve|'s|s|'d|'re|'m|gotten|got|gets|get|getting)") // cf. list in EnglishPTBTreebankCorrector
    }
}

private const val naTag = "NA"
fun startsUpperCase(s: String?): Boolean {
    if (s == null || s.isEmpty()) {
        return false
    }
    val ch = s[0]
    return Character.isUpperCase(ch)
}

/**
 * a string is lowercase if it starts with a lowercase letter
 * such as one from a to z.
 * Should we include numbers?
 * @param s The String to check
 * @return If its first character is lower case
 */
fun startsLowerCase(s: String?): Boolean {
    if (s == null) {
        return false
    }
    val ch = s[0]
    return Character.isLowerCase(ch)
}

fun containsDash(s: String?): Boolean {
    return s != null && s.indexOf('-') >= 0
}

fun containsNumber(s: String?): Boolean {
    if (s == null) {
        return false
    }
    var i = 0
    val len = s.length
    while (i < len) {
        if (Character.isDigit(s[i])) {
            return true
        }
        i++
    }
    return false
}

fun containsLetter(s: String?): Boolean {
    if (s == null) {
        return false
    }
    var i = 0
    val len = s.length
    while (i < len) {
        if (Character.isLetter(s[i])) {
            return true
        }
        i++
    }
    return false
}

fun containsUpperCase(s: String?): Boolean {
    if (s == null) {
        return false
    }
    var i = 0
    val len = s.length
    while (i < len) {
        if (Character.isUpperCase(s[i])) {
            return true
        }
        i++
    }
    return false
}

fun allUpperCase(s: String?): Boolean {
    if (s == null) {
        return false
    }
    var i = 0
    val len = s.length
    while (i < len) {
        if (!Character.isUpperCase(s[i])) {
            return false
        }
        i++
    }
    return true
}

fun noneLowerCase(s: String?): Boolean {
    if (s == null) {
        return false
    }
    var i = 0
    val len = s.length
    while (i < len) {
        if (Character.isLowerCase(s[i])) {
            return false
        }
        i++
    }
    return true
}