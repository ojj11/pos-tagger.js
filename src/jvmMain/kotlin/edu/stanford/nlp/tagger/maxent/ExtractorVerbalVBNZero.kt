package edu.stanford.nlp.tagger.maxent

/**
 * Look for verbs selecting a VBN verb.
 * This is now a zeroeth order observed data only feature.
 * But reminiscent of what was done in Toutanova and Manning 2000.
 * It doesn't seem to help tagging performance any more.
 *
 * @author Christopher Manning
 */
@Suppress("unused")
class ExtractorVerbalVBNZero(val bound: Int) : DictionaryExtractor() {

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
        private const val serialVersionUID = -5881204185400060636L
    }
}
