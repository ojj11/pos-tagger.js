package edu.stanford.nlp

import edu.stanford.nlp.io.PureModel
import edu.stanford.nlp.io.PureTaggerConfig
import edu.stanford.nlp.maxent.Convert
import edu.stanford.nlp.tagger.maxent.*
import java.io.ObjectInputStream

object JavaLoadingTools {

    fun TaggerConfig.taggerConfig(): PureTaggerConfig {
        return PureTaggerConfig(
            defaultScore,
            learnClosedClassTags,
            closedClassTags,
            openClassTags,
            lang,
            rareWordThresh)
    }

    fun fromFile(modelFile: String): PureModel {
        val rf = TaggerConfig.getTaggerDataInputStream(modelFile)
        TaggerConfig.readConfig(rf)
        rf.readInt()
        val ySize = rf.readInt()

        val len = rf.readInt()
        val dict = Dictionary(
                (0 until len).map {
                    val word = rf.readUTF()
                    val numTags = rf.readInt()
                    val tagMap = (0 until numTags).map {
                        var tag: String? = rf.readUTF()
                        val count = rf.readInt()
                        if (tag == TagCount.NULL_SYMBOL) {
                            tag = null
                        }
                        tag to count
                    }.toMap()
                    val tC = TagCount(tagMap)
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
            val fK = FeatureKey(num, value, tag)
            Pair(fK, numF)
        }.toMap()

        val funsize = rf.readInt()
        val b = ByteArray(funsize * 8)
        if (rf.read(b) != b.size) {
            throw IllegalStateException("Lambda values are incomplete")
        }
        val lambda = Convert.byteArrToDoubleArr(b)
        val lambdaSolve = LambdaSolveTagger(lambda)

        rf.close()

        return PureModel(
                ySize,
                PureExtractors(extractors.v.convert()),
                PureExtractors(extractorsRare.v.convert()),
                fAssociations,
                lambdaSolve,
                dict,
                tags,
                closedClassTags
        )
    }

    private fun convertExtractor(extractor: Extractor): PureExtractor {
        return when(extractor) {
            is ExtractorWordLowerCase -> PureExtractorWordLowerCase(extractor.position)
            is ExtractorTwoWords -> PureExtractorTwoWords(extractor.position)
            is ExtractorCWordNextWord -> PureExtractorCWordNextWord()
            is ExtractorCWordPrevWord -> PureExtractorCWordPrevWord()
            is ExtractorPrevTwoTags -> PureExtractorPrevTwoTags()
            is ExtractorPrevThreeTags -> PureExtractorPrevThreeTags()
            is ExtractorNextTwoTags -> PureExtractorNextTwoTags()
            is ExtractorPrevTagWord -> PureExtractorPrevTagWord()
            is ExtractorPrevTagNextTag -> PureExtractorPrevTagNextTag()
            is ExtractorNextTagWord -> PureExtractorNextTagWord()
            is CompanyNameDetector -> PureCompanyNameDetector()
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
            is ExtractorsConjunction -> PureExtractorsConjunction()
            is ExtractorWordShapeClassifier -> PureExtractorWordShapeClassifier(extractor.position, extractor.wsc)
            is ExtractorWordShapeConjunction -> PureExtractorWordShapeConjunction(extractor.left, extractor.right)
            is DictionaryExtractor -> PureDictionaryExtractor()
            else -> PureExtractorBasic(extractor.position, extractor.isDynamic)
        }
    }

    private fun Array<Extractor>.convert(): Array<PureExtractor> = map { convertExtractor(it) }.toTypedArray()
}

