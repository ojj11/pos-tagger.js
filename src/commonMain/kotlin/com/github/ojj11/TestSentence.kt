package com.github.ojj11

import com.github.ojj11.ArrayMath.logSum
import com.github.ojj11.ArrayMath.pairwiseAddInPlace
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

data class SequenceData(
        val sentence: Array<String>,
        val size: Int = sentence.size,
        val pairs: PairsHolder,
        val localScores: MutableMap<String?, DoubleArray> = mutableMapOf(),
        val localContextScores: Array<DoubleArray?>
)

class TestSentence(private val maxentTagger: MaxentTagger) {

    fun tagSentence(s: Array<String>): Array<TaggedWord> {
        val sentence = s + eosWord
        val size = sentence.size
        val pairs = PairsHolder(sentence)

        val bestTags = ExactBestSequenceFinder().bestSequence(
                SequenceData(
                        sentence = sentence,
                        pairs = pairs,
                        localContextScores = arrayOfNulls(size)),
                this)

        val finalTags = (0 until size - 1).map { maxentTagger.tags.getTag(bestTags[it + leftWindow()]) }
        return sentence.zip(finalTags).take(size - 1).map { TaggedWord(it.first, it.second) }.toTypedArray()
    }

    private fun setHistory(current: Int, tags: IntArray, data: SequenceData) {
        // writes over the tags in the last thing in pairs
        val left = leftWindow()
        val right = rightWindow()
        val maxLeft = max(current - left, left)
        val minRight = min(current + right, data.size + left)
        for (j in maxLeft..minRight) {
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
        return DoubleArray(tags.size) {
            histories[maxentTagger.tags.getIndex(tags[it])]
        }
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
        val rare = maxentTagger.dict.sum(cWord.extract(pairs)) < maxentTagger.rareWordThresh
        val ex = maxentTagger.extractors
        val exR = maxentTagger.extractorsRare
        val w = pairs.getWord(0)
        val lS = localScores[w] ?: kotlin.run {
            val out = getHistories(
                    tags, data, ex.local, if (rare) exR.local else null)
            localScores[w] = out
            out
        }
        val lcS = localContextScores[pairs.offset] ?: kotlin.run {
            val out = getHistories(
                    tags, data, ex.localContext, if (rare) exR.localContext else null)
            localContextScores[pairs.offset] = out
            pairwiseAddInPlace(out, lS)
            out
        }
        val totalS = getHistories(
                tags, data, ex.dynamic, if (rare) exR.dynamic else null)
        pairwiseAddInPlace(totalS, lcS)
        return totalS
    }

    private fun getHistories(
            tags: Array<String>,
            data: SequenceData,
            extractors: Map<Int, PureExtractor>,
            extractorsRare: Map<Int, PureExtractor>?
    ): DoubleArray {

        val tags = if (maxentTagger.defaultScore > 0) {
            tags
        } else {
            Array(maxentTagger.ySize) {
                maxentTagger.tags.getTag(it)
            }
        }

        val rareOffset = maxentTagger.extractors.size

        val extractorsCombined = extractors + (extractorsRare?.mapKeys {
            it.key + rareOffset
        } ?: emptyMap())

        val extractorsEvaluated = extractorsCombined.mapValues {
            it.value.extract(data.pairs)
        }

        return DoubleArray(tags.size) {
            var score = 0.0
            for ((kf, ex) in extractorsEvaluated) {
                maxentTagger.features[Feature(kf, ex, tags[it])]?.let { i ->
                    score += maxentTagger.lambda[i]
                }
            }

            score
        }
    }

    fun leftWindow() = maxentTagger.leftContext

    fun rightWindow() = maxentTagger.rightContext

    fun getPossibleValues(pos: Int, data: SequenceData): IntArray {
        val arr1 = stringTagsAt(pos, data)
        return IntArray(arr1.size) {
            maxentTagger.tags.getIndex(arr1[it])
        }
    }

    fun scoresOf(tags: IntArray, pos: Int, data: SequenceData): DoubleArray {
        data.pairs.offset = pos - leftWindow()
        setHistory(pos, tags, data)
        return getScores(data)
    }

    private fun stringTagsAt(pos: Int, data: SequenceData): Array<String> {
        return when {
            pos < leftWindow() || pos >= data.size + leftWindow() ->
                naTagSet
            maxentTagger.dict.isUnknown(data.sentence[pos - leftWindow()]) ->
                maxentTagger.tags.openClassTags
            else ->
                maxentTagger.dict.getTags(data.sentence[pos - leftWindow()])
        }
    }
}

private const val eosWord = "EOS"
private const val naTag = "NA"
private val naTagSet = arrayOf(naTag)
