package edu.stanford.nlp.tagger.maxent

import com.github.ojj11.Dictionary
import com.github.ojj11.WordShapeClassifier

/**
 * Superclass for rare word feature frames.  Provides some common functions.
 * Designed to be extended.
 */
@Suppress("unused")
internal open class RareExtractor : Extractor() {
    companion object {
        private const val serialVersionUID = -7682607870855426599L
    }
}

/** English-specific crude company name NER.  */
@Suppress("unused")
internal class CompanyNameDetector(val companyNameEnds: MutableSet<String?>) : Extractor() {
    companion object {
        private const val serialVersionUID = 21L
    }
} // end class CompanyNameDetector

@Suppress("unused")
internal class ExtractorUCase : Extractor() {

    companion object {
        private const val serialVersionUID = 22L
    }
}

@Suppress("unused")
internal class ExtractorLetterDigitDash : Extractor() {

    companion object {
        private const val serialVersionUID: Long = 23
    }
}

@Suppress("unused")
internal class ExtractorUpperDigitDash : Extractor() {

    companion object {
        private const val serialVersionUID = 33L
    }
}

/**
 * creates features which are true if the current word is all caps
 * and the distance to the first lowercase word to the left is dist
 * the distance is 1 for adjacent, 2 for one across, 3 for ... and so on.
 * inifinity if no capitalized word (we hit the start of sentence or '')
 */
@Suppress("unused")
internal class ExtractorCapDistLC : Extractor() {

    companion object {
        private const val serialVersionUID = 34L
    }
}

/**
 * This feature applies when the word is capitalized
 * and the previous lower case is infinity
 * and the lower cased version of it has occured 2 or more times with tag t
 * false if the word was not seen.
 * create features only for tags that are the same as the tag t
 */
@Suppress("unused")
internal class ExtractorCapLCSeen(val tag: String?) : Extractor() {
    private val cutoff = 1
    private val cCapDist: Extractor = ExtractorCapDistLC()

    @Transient
    private lateinit var dict: Dictionary

    companion object {
        private const val serialVersionUID = 35L
    }
}

/**
 * "1" if not first word of sentence and _some_ letter is uppercase
 */
@Suppress("unused")
internal class ExtractorMidSentenceCap : Extractor() {

    companion object {
        private const val serialVersionUID = 24L
    }
}

/**
 * "0" if not 1st word of sentence or not upper case, or lowercased version
 * not in dictionary.  Else first tag of word lowercased.
 */
@Suppress("unused")
internal class ExtractorStartSentenceCap : Extractor() {

    @Transient
    private lateinit var dict: Dictionary

    companion object {
        private const val serialVersionUID = 25L
    }
}

/**
 * "0" if first word of sentence or not first letter uppercase or if
 * lowercase version isn't in dictionary.  Otherwise first tag of lowercase
 * equivalent.
 */
@Suppress("unused")
internal class ExtractorMidSentenceCapC : Extractor() {

    @Transient
    private lateinit var dict: Dictionary

    companion object {
        private const val serialVersionUID = 26L
    }
} // TODO: the next time we have to rebuild the tagger files anyway, we

// should change this class's name to something like
// "ExtractorNoLowercase" to distinguish it from
// ExtractorAllCapitalized
@Suppress("unused")
internal class ExtractorAllCap : Extractor() {

    companion object {
        private const val serialVersionUID = 27L
    }
}

@Suppress("unused")
internal class ExtractorAllCapitalized : Extractor() {

    companion object {
        private const val serialVersionUID = 32L
    }
}

@Suppress("unused")
internal class ExtractorCNumber : Extractor() {

    companion object {
        private const val serialVersionUID = 28L
    }
}

@Suppress("unused")
internal class ExtractorDash : Extractor() {

    companion object {
        private const val serialVersionUID = 29L
    }
}

@Suppress("unused")
internal class ExtractorCWordSuff(val num: Int) : Extractor() {

    override fun toString() = super.toString() + " size " + num

    companion object {
        private const val serialVersionUID = 30L
    }
}

@Suppress("unused")
internal class ExtractorCWordPref(val num: Int) : Extractor() {

    override fun toString() = super.toString() + " size " + num

    companion object {
        private const val serialVersionUID = 31L
    }
} // end class ExtractorCWordPref

@Suppress("unused")
internal class ExtractorsConjunction(val extractor1: Extractor, val extractor2: Extractor) : Extractor() {
    private var privateIsLocal: Boolean = false
    private var privateIsDynamic: Boolean = false

    companion object {
        private const val serialVersionUID = 36L
    }
}

@Suppress("unused")
internal class ExtractorWordShapeClassifier(position: Int, val wordShaper: Int) : Extractor(position, false) {

    override fun toString(): String {
        return super.toString() + " " + wordShaper
    }

    companion object {
        private const val serialVersionUID = 101L
    }
}

/**
 * This extractor extracts a conjunction of word shapes.
 */
@Suppress("unused")
internal class ExtractorWordShapeConjunction(val left: Int, val right: Int, val wordShaper: Int = WordShapeClassifier.lookupShaper("chris4")) : Extractor() {
    private val name: String = "ExtractorWordShapeConjunction($left,$right)"

    companion object {
        private const val serialVersionUID = -49L
    }
}

internal class CTBunkDictDetector(private val t1: String, private val n1: Int) : RareExtractor() {
    companion object {
        private const val serialVersionUID = 80L
    }
} // end class CTBunkDictDetector

internal class ASBCunkDetector(private val t1: String, private val n1: Int) : RareExtractor() {
    companion object {
        private const val serialVersionUID = 57L
    }
} // end class ASBCunkDetector

internal class CtbSufDetector(private val t1: String, n2: Int) : RareExtractor() {

    override fun toString() = super.toString() + " tag=" + t1

    companion object {
        private const val serialVersionUID = 44L
    }
} // end class ctbPreDetector

internal class CtbPreDetector(private val t1: String, n2: Int) : RareExtractor() {

    override fun toString() = super.toString() + " tag=" + t1

    companion object {
        private const val serialVersionUID = 43L
    }
} // end class ctbPreDetector

internal class PluralAcronymDetector : RareExtractor() {
    companion object {
        private const val serialVersionUID = 33L
    }
}
