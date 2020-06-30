package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.ling.TaggedWord
import edu.stanford.nlp.ling.Word
import edu.stanford.nlp.math.ArrayMath
import edu.stanford.nlp.math.ArrayMath.logSum
import edu.stanford.nlp.math.ArrayMath.pairwiseAddInPlace
import edu.stanford.nlp.sequences.ExactBestSequenceFinder
import kotlin.math.ln

data class SequenceData(
        val sentence: List<String>,
        val size: Int = sentence.size,
        val pairs: PairsHolder,
        val localScores: MutableMap<String?, DoubleArray> = mutableMapOf(),
        val localContextScores: MutableMap<Int, DoubleArray> = mutableMapOf()
)

class TestSentence(private val maxentTagger: MaxentTagger) {

    fun tagSentence(s: List<Word>): List<TaggedWord> {
        val sentence = s.map { it.word } + eosWord
        val size = sentence.size
        val pairs = PairsHolder(sentence)

        val bestTags = ExactBestSequenceFinder().bestSequence(
                SequenceData(
                        sentence = sentence,
                        pairs = pairs),
                this)

        val finalTags = (0 until size - 1).map { maxentTagger.tags.getTag(bestTags[it + leftWindow()]) }
        return sentence.zip(finalTags).take(size - 1).map { TaggedWord(it.first, it.second) }
    }

    private fun setHistory(current: Int, tags: IntArray, data: SequenceData) {
        // writes over the tags in the last thing in pairs
        val left = leftWindow()
        val right = rightWindow()
        val range = current - left..current + right
        val closedRange = range.intersect(left until data.size + left)
        for (j in closedRange) {
            data.pairs[j - left] = maxentTagger.tags.getTag(tags[j])
        }
    }

    private fun getScores(data: SequenceData): DoubleArray {
        return if (maxentTagger.defaultScore > 0) {
            getApproximateScores(data)
        } else {
            getExactScores(data)
        }
    }

    private fun getExactScores(data: SequenceData): DoubleArray {
        val tags = stringTagsAt(data.pairs.offset + leftWindow(), data)
        val histories = getHistories(tags, data)
        ArrayMath.logNormalize(histories)
        return tags.map { histories[maxentTagger.tags.getIndex(it)] }.toDoubleArray()
    }

    private fun getApproximateScores(data: SequenceData): DoubleArray {
        val tags = stringTagsAt(data.pairs.offset + leftWindow(), data)
        val scores = getHistories(tags, data)

        // Number of tags that get assigned a default score:
        val nDefault = maxentTagger.ySize - tags.size.toDouble()
        val logScore: Double = logSum(scores)
        val logScoreInactiveTags = ln(nDefault * maxentTagger.defaultScore)
        val logTotal: Double = logSum(doubleArrayOf(logScore, logScoreInactiveTags))
        ArrayMath.addInPlace(scores, -logTotal)
        return scores
    }

    private fun getHistories(tags: Array<String>, data: SequenceData): DoubleArray {
        val pairs = data.pairs
        val localScores = data.localScores
        val localContextScores = data.localContextScores
        val rare = maxentTagger.isRare(cWord.extract(pairs))
        val ex = maxentTagger.extractors
        val exR = maxentTagger.extractorsRare
        val w = pairs.getWord(0)
        val lS = localScores[w] ?: kotlin.run {
            val out = getHistories(tags, data, ex.local, if (rare) exR.local else null)
            localScores[w] = out
            out
        }
        val lcS = localContextScores[pairs.offset] ?: kotlin.run {
            val out = getHistories(tags, data, ex.localContext, if (rare) exR.localContext else null)
            localContextScores[pairs.offset] = out
            pairwiseAddInPlace(out, lS)
            out
        }
        val totalS = getHistories(tags, data, ex.dynamic, if (rare) exR.dynamic else null)
        pairwiseAddInPlace(totalS, lcS)
        return totalS
    }

    private fun getHistories(
            tags: Array<String>,
            data: SequenceData,
            extractors: Map<Int, PureExtractor>,
            extractorsRare: Map<Int, PureExtractor>?
    ): DoubleArray {

        val isApproximate = maxentTagger.defaultScore > 0
        val scores = if (isApproximate) {
            DoubleArray(maxentTagger.ySize)
        } else {
            DoubleArray(tags.size)
        }
        val commonSize = maxentTagger.extractors.size
        val rareExtractors = extractorsRare?.map {
            it.key + commonSize to it.value
        } ?: emptyList()
        val combinedExtractors = extractors + rareExtractors
        for ((index, extractor) in combinedExtractors) {
            val range = if (isApproximate) {
                scores.indices.map { maxentTagger.tags.getTag(it) }
            } else {
                tags.toList()
            }
            for ((i, tag) in range.withIndex()) {
                val featureIndex = maxentTagger.getFeature(
                        Feature(index, extractor.extract(data.pairs), tag))
                if (featureIndex > -1) {
                    scores[i] += maxentTagger.lambda[featureIndex]
                }
            }
        }
        return scores
    }


    fun leftWindow() = maxentTagger.leftContext

    fun rightWindow() = maxentTagger.rightContext

    fun getPossibleValues(pos: Int, data: SequenceData): IntArray {
        val arr1 = stringTagsAt(pos, data)
        return arr1.indices.map { maxentTagger.tags.getIndex(arr1[it]) }.toIntArray()
    }

    fun scoresOf(tags: IntArray, pos: Int, data: SequenceData): DoubleArray {
        data.pairs.offset = pos - leftWindow()
        setHistory(pos, tags, data)
        return getScores(data)
    }

    private fun stringTagsAt(pos: Int, data: SequenceData): Array<String> {
        return when {
            pos < leftWindow() || pos >= data.size + leftWindow() -> {
                arrayOf(naTag)
            }
            maxentTagger.dict.isUnknown(data.sentence[pos - leftWindow()]) -> {
                maxentTagger.tags.deterministicallyExpandTags(
                        maxentTagger.tags.openClassTags.toTypedArray(),
                        data.sentence[pos - leftWindow()])
            }
            else -> {
                maxentTagger.tags.deterministicallyExpandTags(
                        maxentTagger.dict.getTags(data.sentence[pos - leftWindow()]),
                        data.sentence[pos - leftWindow()])
            }
        }
    }
}

private const val eosWord = "EOS"
private const val naTag = "NA"