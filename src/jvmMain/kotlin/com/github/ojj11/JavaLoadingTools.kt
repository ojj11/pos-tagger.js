package com.github.ojj11

import edu.stanford.nlp.maxent.Convert
import edu.stanford.nlp.tagger.maxent.CompanyNameDetector
import edu.stanford.nlp.tagger.maxent.DictionaryExtractor
import edu.stanford.nlp.tagger.maxent.Extractor
import edu.stanford.nlp.tagger.maxent.ExtractorAllCap
import edu.stanford.nlp.tagger.maxent.ExtractorAllCapitalized
import edu.stanford.nlp.tagger.maxent.ExtractorCNumber
import edu.stanford.nlp.tagger.maxent.ExtractorCWordNextWord
import edu.stanford.nlp.tagger.maxent.ExtractorCWordPref
import edu.stanford.nlp.tagger.maxent.ExtractorCWordPrevWord
import edu.stanford.nlp.tagger.maxent.ExtractorCWordSuff
import edu.stanford.nlp.tagger.maxent.ExtractorCapDistLC
import edu.stanford.nlp.tagger.maxent.ExtractorCapLCSeen
import edu.stanford.nlp.tagger.maxent.ExtractorDash
import edu.stanford.nlp.tagger.maxent.ExtractorLetterDigitDash
import edu.stanford.nlp.tagger.maxent.ExtractorMidSentenceCap
import edu.stanford.nlp.tagger.maxent.ExtractorMidSentenceCapC
import edu.stanford.nlp.tagger.maxent.ExtractorNextTagWord
import edu.stanford.nlp.tagger.maxent.ExtractorNextTwoTags
import edu.stanford.nlp.tagger.maxent.ExtractorPrevTagNextTag
import edu.stanford.nlp.tagger.maxent.ExtractorPrevTagWord
import edu.stanford.nlp.tagger.maxent.ExtractorPrevThreeTags
import edu.stanford.nlp.tagger.maxent.ExtractorPrevTwoTags
import edu.stanford.nlp.tagger.maxent.ExtractorStartSentenceCap
import edu.stanford.nlp.tagger.maxent.ExtractorTwoWords
import edu.stanford.nlp.tagger.maxent.ExtractorUCase
import edu.stanford.nlp.tagger.maxent.ExtractorUpperDigitDash
import edu.stanford.nlp.tagger.maxent.ExtractorVerbalVBNZero
import edu.stanford.nlp.tagger.maxent.ExtractorWordLowerCase
import edu.stanford.nlp.tagger.maxent.ExtractorWordShapeClassifier
import edu.stanford.nlp.tagger.maxent.ExtractorWordShapeConjunction
import edu.stanford.nlp.tagger.maxent.Extractors
import edu.stanford.nlp.tagger.maxent.ExtractorsConjunction
import edu.stanford.nlp.tagger.maxent.TaggerConfig
import java.io.ObjectInputStream

data class UnoptimisedFeature(
    val extractorIndex: Int,
    val extractedValue: String,
    val tag: String
)

object JavaLoadingTools {

    /** code to handle marshaling from [ObjectInputStream] into the pure models */
    fun load(path: String): PureParameters {

        val config = TaggerConfig("-model", path)
        val rf = TaggerConfig.getTaggerDataInputStream(path)
        TaggerConfig.readConfig(rf)
        rf.readInt()
        val ySize = rf.readInt()
        val len = rf.readInt()
        val dict = Dictionary(
            (0 until len).map {
                val word = rf.readUTF()
                val numTags = rf.readInt()
                val tagMap = (0 until numTags).map {
                    var tag = rf.readUTF()
                    val count = rf.readInt()
                    if (tag == "<<NULL>>") {
                        tag = null
                    }
                    tag to count
                }
                val existingTags = tagMap.map { it.first }.toTypedArray()
                val missingTags = (
                    deterministicallyExpandTags(config.lang, existingTags).toSet() - existingTags.toSet()
                    ).map {
                    it to 0
                }

                val tC = TagCount((tagMap + missingTags).toMap())
                word to tC
            }.toMap()
        )
        val len1 = rf.readInt()
        for (i in 0 until len1) {
            rf.readInt()
            val len = rf.readInt()
            val buff = ByteArray(len)
            if (rf.read(buff) != len) {
                throw IllegalStateException("rewrite")
            }
            rf.readInt()
            rf.readInt()
            rf.readInt()
            rf.readInt()
        }
        val tempTags = (0 until rf.readInt()).map {
            val tag = rf.readUTF()
            val inClosed = rf.readBoolean()
            Pair(tag, inClosed)
        }
        val tags = tempTags.map { it.first }
        val closedClassTags = tempTags.filter { it.second }.map { it.first }
        val `in` = ObjectInputStream(rf)
        val extractors = `in`.readObject() as Extractors
        val extractorsRare = `in`.readObject() as Extractors
        val sizeAssoc = rf.readInt()
        val fAssociations = (0 until sizeAssoc).map {
            val numF = rf.readInt()
            val num = rf.readInt()
            // mg2008: slight speedup:
            val value = rf.readUTF()
            // intern the tag strings as they are read, since there are few of them. This saves tons of memory.
            val tag = rf.readUTF()
            val fK = UnoptimisedFeature(num, value, tag)
            fK to numF
        }.toMap()

        val optimisedFeatures = fAssociations.entries.groupBy {
            Feature(it.key.extractorIndex, it.key.extractedValue)
        }.mapValues {
            it.value.map {
                it.key.tag to it.value
            }.toMap()
        }

        val funsize = rf.readInt()
        val b = ByteArray(funsize * 8)
        if (rf.read(b) != b.size) {
            throw IllegalStateException("Lambda values are incomplete")
        }
        val lambda = Convert.byteArrToDoubleArr(b)
        rf.close()

        val open = when {
            config.openClassTags.isNotEmpty() -> {
                deterministicallyExpandTags(config.lang, config.openClassTags).toSet()
            }
            closedClassTags.isNotEmpty() -> {
                val closed = config.closedClassTags.toSet()
                tags.filter { it !in closed }.toSet()
            }
            else -> {
                val closed = defaultClosedTags(config.lang)
                tags.filter { it !in closed }.toSet()
            }
        }

        return PureParameters(
            ySize,
            PureExtractors(extractors.v.convert()),
            PureExtractors(extractorsRare.v.convert()),
            lambda,
            dict,
            TTags(tags.toTypedArray(), open.toTypedArray()),
            config.defaultScore,
            config.learnClosedClassTags,
            config.lang,
            config.rareWordThresh,
            optimisedFeatures
        )
    }

    private fun convertExtractor(extractor: Extractor): PureExtractor {
        return when (extractor) {
            is ExtractorVerbalVBNZero -> PureExtractorVerbalVBNZero(extractor.bound)
            is ExtractorWordLowerCase -> PureExtractorWordLowerCase(extractor.position)
            is ExtractorTwoWords -> PureExtractorTwoWords(extractor.leftPosition)
            is ExtractorCWordNextWord -> PureExtractorCWordNextWord()
            is ExtractorCWordPrevWord -> PureExtractorCWordPrevWord()
            is ExtractorPrevTwoTags -> PureExtractorPrevTwoTags()
            is ExtractorPrevThreeTags -> PureExtractorPrevThreeTags()
            is ExtractorNextTwoTags -> PureExtractorNextTwoTags()
            is ExtractorPrevTagWord -> PureExtractorPrevTagWord()
            is ExtractorPrevTagNextTag -> PureExtractorPrevTagNextTag()
            is ExtractorNextTagWord -> PureExtractorNextTagWord()
            is CompanyNameDetector -> PureCompanyNameDetector(extractor.companyNameEnds)
            is ExtractorUCase -> PureExtractorUCase()
            is ExtractorLetterDigitDash -> PureExtractorLetterDigitDash()
            is ExtractorUpperDigitDash -> PureExtractorUpperDigitDash()
            is ExtractorCapDistLC -> PureExtractorCapDistLC()
            is ExtractorCapLCSeen -> PureExtractorCapLCSeen(extractor.tag)
            is ExtractorMidSentenceCap -> PureExtractorMidSentenceCap()
            is ExtractorStartSentenceCap -> PureExtractorStartSentenceCap()
            is ExtractorMidSentenceCapC -> PureExtractorMidSentenceCapC()
            is ExtractorAllCap -> PureExtractorAllCap()
            is ExtractorAllCapitalized -> PureExtractorAllCapitalized()
            is ExtractorCNumber -> PureExtractorCNumber()
            is ExtractorDash -> PureExtractorDash()
            is ExtractorCWordSuff -> PureExtractorCWordSuff(extractor.num)
            is ExtractorCWordPref -> PureExtractorCWordPref(extractor.num)
            is ExtractorsConjunction -> PureExtractorsConjunction(
                convertExtractor(extractor.extractor1),
                convertExtractor(extractor.extractor2)
            )
            is ExtractorWordShapeClassifier -> PureExtractorWordShapeClassifier(extractor.position, extractor.wordShaper)
            is ExtractorWordShapeConjunction -> PureExtractorWordShapeConjunction(extractor.left, extractor.right, extractor.wordShaper)
            is DictionaryExtractor -> PureDictionaryExtractor()
            else -> PureExtractorBasic(extractor.position, extractor.isTag)
        }
    }

    private fun Array<Extractor>.convert(): Array<PureExtractor> = map { convertExtractor(it) }.toTypedArray()
}

fun deterministicallyExpandTags(lang: String, tags: Array<String>): Array<String> {
    return if (lang.equals("english", ignoreCase = true)) {
        var tags = tags
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
        tags
    } else {
        // no tag expansion for other languages currently
        tags
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
