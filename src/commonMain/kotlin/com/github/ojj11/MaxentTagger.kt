package com.github.ojj11

import com.github.ojj11.ArrayMath.logSum
import com.github.ojj11.ArrayMath.logSumTwoNumbers
import com.github.ojj11.ArrayMath.pairwiseAddInPlace
import kotlin.math.ln
import kotlin.math.max

data class SequenceData(
        val sentence: Array<String>,
        val size: Int = sentence.size,
        val pairs: PairsHolder,
        val localScores: Array<DoubleArray?>,
        val localContextScores: Array<DoubleArray?>,
        val dictionary: Dictionary,
        val rare: BooleanArray
)

class MaxentTagger(private val parameters: PureParameters) {

    private val leftContext = max(
            parameters.extractors.leftContext(),
            parameters.extractorsRare.leftContext())

    private val rightContext = max(
            parameters.extractors.rightContext(),
            parameters.extractorsRare.rightContext())

    private val defaultScore = when {
        parameters.defaultScore >= 0 -> parameters.defaultScore
        parameters.lang == "english" -> 1.0
        else -> 0.0
    }

    private val allExtractors = parameters.extractors.combine(parameters.extractorsRare)

    fun tagSentence(s: Array<String>): Array<TaggedWord> {
        val sentence = s + eosWord
        val size = sentence.size
        val pairs = PairsHolder(sentence)

        val rare = BooleanArray(size) {
            parameters.dict.sum(sentence[it]) < parameters.rareWordThresh
        }

        val bestTags = bestSequence(
                SequenceData(
                        sentence = sentence,
                        pairs = pairs,
                        localScores = arrayOfNulls(size),
                        localContextScores = arrayOfNulls(size),
                        dictionary = parameters.dict,
                        rare = rare))

        val finalTags = Array(size - 1) {
            parameters.tags.getTag(bestTags[it + leftContext])
        }
        return Array(size - 1) {
            TaggedWord(sentence[it], finalTags[it])
        }
    }

    private fun setHistory(current: Int, tags: IntArray, data: SequenceData) {
        // writes over the tags in the last thing in pairs
        val leftA = current - leftContext
        val maxLeft = if (leftA > leftContext) {
            leftA
        } else {
            leftContext
        }
        val rightA = current + rightContext
        val rightB = data.size + leftContext
        val minRight = if (rightA < rightB) {
            rightA
        } else {
            rightB
        }
        for (j in maxLeft..minRight) {
            data.pairs[j - leftContext] = parameters.tags.getTag(tags[j])
        }
    }

    private fun getScores(data: SequenceData): DoubleArray {
        return if (defaultScore > 0) {
            getApproximateScores(data)
        } else {
            getExactScores(data)
        }
    }

    private fun getExactScores(data: SequenceData): DoubleArray {
        val tags = stringTagsAt(data.pairs.offset + leftContext, data)
        val histories = getHistories(data, parameters.tags.tags)
        ArrayMath.logNormalize(histories)
        return DoubleArray(tags.size) {
            histories[parameters.tags.getIndex(tags[it])]
        }
    }

    private fun getApproximateScores(data: SequenceData): DoubleArray {
        val tags = stringTagsAt(data.pairs.offset + leftContext, data)
        val scores = getHistories(data, tags)

        // Number of tags that get assigned a default score:
        val nDefault = parameters.ySize - tags.size.toDouble()
        val logScore = logSum(scores)
        val logScoreInactiveTags = ln(nDefault * defaultScore)
        val logTotal = logSumTwoNumbers(logScore, logScoreInactiveTags)
        ArrayMath.addInPlace(scores, -logTotal)
        return scores
    }

    private fun getHistories(data: SequenceData, modifiedTags: Array<String>): DoubleArray {
        val pairs = data.pairs
        val localScores = data.localScores
        val localContextScores = data.localContextScores
        val rare = data.rare[data.pairs.offset]
        val w = pairs.getWord(0)
        val commonWordExtractors = parameters.extractors
        val lS = localScores[pairs.offset] ?: kotlin.run {
            val out = getHistories(
                    modifiedTags, data, if (rare) allExtractors.local else commonWordExtractors.local)
            localScores[pairs.offset] = out
            out
        }
        val lcS = localContextScores[pairs.offset] ?: kotlin.run {
            val out = getHistories(
                    modifiedTags, data, if (rare) allExtractors.localContext else commonWordExtractors.localContext)
            localContextScores[pairs.offset] = out
            pairwiseAddInPlace(out, lS)
            out
        }
        val totalS = getHistories(
                modifiedTags, data, if (rare) allExtractors.dynamic else commonWordExtractors.dynamic)
        pairwiseAddInPlace(totalS, lcS)
        return totalS
    }

    private fun getHistories(
            tags: Array<String>,
            data: SequenceData,
            extractors: Array<Pair<Int, PureExtractor>>
    ): DoubleArray {

        val extractorsEvaluated = Array(extractors.size) {
            val ex = extractors[it]
            val extractedKey = Feature(ex.first, ex.second.extract(data.pairs, data.dictionary))
            parameters.optimisedFeatures[extractedKey]
        }

        return DoubleArray(tags.size) {
            var score = 0.0
            for (e in extractorsEvaluated) {
                e?.get(tags[it])?.let { i ->
                    score += parameters.lambda[i]
                }
            }

            score
        }
    }

    private fun getPossibleValues(pos: Int, data: SequenceData): IntArray {
        val arr1 = stringTagsAt(pos, data)
        return IntArray(arr1.size) {
            parameters.tags.getIndex(arr1[it])
        }
    }

    private fun scoresOf(tags: IntArray, pos: Int, data: SequenceData): DoubleArray {
        data.pairs.offset = pos - leftContext
        setHistory(pos, tags, data)
        return getScores(data)
    }

    private fun stringTagsAt(pos: Int, data: SequenceData): Array<String> {
        return when {
            pos < leftContext || pos >= data.size + leftContext ->
                naTagSet
            parameters.dict.isUnknown(data.sentence[pos - leftContext]) ->
                parameters.tags.openClassTags
            else ->
                parameters.dict.getTags(data.sentence[pos - leftContext])
        }
    }

    /** find the best tags for the given sentence, propagates [data] */
    private fun bestSequence(data: SequenceData): IntArray {
        val size = data.sentence.size
        val padLength = size + leftContext + rightContext
        val tags = Array(padLength) { getPossibleValues(it, data) }
        val tagNum = IntArray(padLength) { tags[it].size }
        val tempTags = IntArray(padLength)
        val productSizes = IntArray(padLength)
        var curProduct = 1
        for (i in 0 until leftContext + rightContext) {
            curProduct *= tagNum[i]
        }
        for (pos in leftContext + rightContext until padLength) {
            if (pos > leftContext + rightContext) {
                curProduct /= tagNum[pos - leftContext - rightContext - 1] // shift off
            }
            curProduct *= tagNum[pos] // shift on
            productSizes[pos - rightContext] = curProduct
        }
        val windowScore = Array(padLength) { DoubleArray(productSizes[it]) }
        for (pos in leftContext until leftContext + size) {
            tempTags.fill(tags[0][0])
            for (product in 0 until productSizes[pos]) {
                var p = product
                var shift = 1
                for (curPos in pos + rightContext downTo pos - leftContext) {
                    tempTags[curPos] = tags[curPos][p % tagNum[curPos]]
                    p /= tagNum[curPos]
                    if (curPos > pos) {
                        shift *= tagNum[curPos]
                    }
                }

                if (tempTags[pos] == tags[pos][0]) {
                    // get all tags at once
                    val scores = scoresOf(tempTags, pos, data)
                    // fill in the relevant windowScores
                    for (t in 0 until tagNum[pos]) {
                        windowScore[pos][product + t * shift] = scores[t]
                    }
                }
            }
        }
        val score = Array(padLength) { DoubleArray(productSizes[it]) }
        val trace = Array(padLength) { IntArray(productSizes[it]) }
        for (pos in leftContext until size + leftContext) {
            val innerScore = score[pos]
            val innerTrace = trace[pos]
            val previousScore = score[pos - 1]
            // loop over window product types
            for (product in 0 until productSizes[pos]) {
                val innerWindowScore = windowScore[pos][product]
                // check for initial spot
                if (pos == leftContext) {
                    // no predecessor type
                    innerScore[product] = innerWindowScore
                    innerTrace[product] = -1
                } else {
                    // loop over possible predecessor types
                    innerScore[product] = Double.NEGATIVE_INFINITY
                    innerTrace[product] = -1
                    val sharedProduct = product / tagNum[pos + rightContext]
                    val factor = productSizes[pos] / tagNum[pos + rightContext]
                    for (newTagNum in 0 until tagNum[pos - leftContext - 1]) {
                        val predProduct = newTagNum * factor + sharedProduct
                        val predScore = previousScore[predProduct] + innerWindowScore
                        if (predScore > innerScore[product]) {
                            innerScore[product] = predScore
                            innerTrace[product] = predProduct
                        }
                    }
                }
            }
        }
        var bestFinalScore = Double.NEGATIVE_INFINITY
        var bestCurrentProduct = -1
        val innerScore = score[leftContext + size - 1]
        for (product in 0 until productSizes[leftContext + size - 1]) {
            if (innerScore[product] > bestFinalScore) {
                bestCurrentProduct = product
                bestFinalScore = innerScore[product]
            }
        }
        var lastProduct = bestCurrentProduct
        val endPos = max(size - 1, 0)
        for (last in padLength - 1 downTo endPos) {
            tempTags[last] = tags[last][lastProduct % tagNum[last]]
            lastProduct /= tagNum[last]
        }
        for (pos in leftContext + size - 2 downTo leftContext) {
            bestCurrentProduct = trace[pos + 1][bestCurrentProduct]
            tempTags[pos - leftContext] = tags[pos - leftContext][bestCurrentProduct / (productSizes[pos] / tagNum[pos - leftContext])]
        }
        return tempTags
    }
}

private const val eosWord = "EOS"
private const val naTag = "NA"
private val naTagSet = arrayOf(naTag)
