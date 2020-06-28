package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.ling.HasWord
import edu.stanford.nlp.ling.TaggedWord
import edu.stanford.nlp.math.ArrayMath
import edu.stanford.nlp.math.ArrayMath.logSum
import edu.stanford.nlp.math.ArrayMath.pairwiseAddInPlace
import edu.stanford.nlp.sequences.ExactBestSequenceFinder
import kotlin.math.ln

data class SequenceData(
        val sentence: List<String>,
        val size: Int = sentence.size,
        val pairs: PairsHolder,
        val history: History,
        val localScores: MutableMap<String?, DoubleArray> = mutableMapOf(),
        val localContextScores: MutableMap<Int, DoubleArray> = mutableMapOf()
)

/**
 * @author Kristina Toutanova
 * @author Michel Galley
 * @version 1.0
 */
class TestSentence(private val maxentTagger: MaxentTagger) {

    /**
     * Tags the sentence s by running maxent model.  Returns a Sentence of
     * TaggedWord objects.
     *
     * @param s Input sentence (List).  This isn't changed.
     * @return Tagged sentence
     */
    fun tagSentence(s: List<HasWord>): List<TaggedWord> {
        val sentence = s.mapNotNull { it.word() } + eosWord
        val size = sentence.size
        val pairs = PairsHolder(sentence)

        val bestTags = ExactBestSequenceFinder().bestSequence(
                SequenceData(
                        sentence = sentence,
                        pairs = pairs,
                        history = History(pairs, maxentTagger.extractors)
                ),
                this
        )

        val finalTags = (0 until size - 1).map { maxentTagger.tags.getTag(bestTags[it + leftWindow()]) }
        return sentence.zip(finalTags).take(size - 1).map { TaggedWord(it.first, it.second) }
    }

    // This is used for Dan's tag inference methods.
    // current is the actual word number + leftW
    private fun setHistory(current: Int, tags: IntArray, data: SequenceData) {
        //writes over the tags in the last thing in pairs
        val left = leftWindow()
        val right = rightWindow()
        for (j in current - left..current + right) {
            if (j < left) {
                continue
            } //but shouldn't happen
            if (j >= data.size + left) {
                break
            } //but shouldn't happen
            data.history.setTag(j - left, maxentTagger.tags.getTag(tags[j]))
        }
    }

    private fun append(tags: Array<String?>, word: String?): Array<String?> {
        return maxentTagger.tags.deterministicallyExpandTags(tags, word)
    }

    // This scores the current assignment in PairsHolder at
    // current position h.current (returns normalized scores)
    private fun getScores(data: SequenceData): DoubleArray {

        return if (maxentTagger.defaultScore > 0) {
            getApproximateScores(data)
        } else {
            getExactScores(data)
        }
    }

    private fun getExactScores(data: SequenceData): DoubleArray {

        val h = data.history
        val tags = stringTagsAt(h.current - h.start + leftWindow(), data)
        val histories = getHistories(tags!!, data)
        ArrayMath.logNormalize(histories)
        return tags.map { histories[maxentTagger.tags.getIndex(it)] }.toDoubleArray()
    }

    // In this method, each tag that is incompatible with the current word
    // (e.g., apple_CC) gets a default (constant) score instead of its exact score.
    // The scores of all other tags are computed exactly.
    private fun getApproximateScores(
            data: SequenceData
    ): DoubleArray {
        val h = data.history
        val tags = stringTagsAt(h.current - h.start + leftWindow(), data)
        val scores = getHistories(tags!!, data) // log score for each active tag, unnormalized

        // Number of tags that get assigned a default score:
        val nDefault = maxentTagger.ySize - tags.size.toDouble()
        val logScore: Double = logSum(scores)
        val logScoreInactiveTags = ln(nDefault * maxentTagger.defaultScore)
        val logTotal: Double = logSum(doubleArrayOf(logScore, logScoreInactiveTags))
        ArrayMath.addInPlace(scores, -logTotal)
        return scores
    }

    // This precomputes scores of local features (localScores).
    private fun getHistories(tags: Array<String?>, data: SequenceData): DoubleArray {
        val h = data.history
        val pairs = data.pairs
        val localScores = data.localScores
        val localContextScores = data.localContextScores
        val rare = maxentTagger.isRare(cWord.extract(h))
        val ex = maxentTagger.extractors
        val exR = maxentTagger.extractorsRare
        val w = pairs.getWord(h.current)
        val lS = localScores[w] ?: kotlin.run {
            val out = getHistories(tags, h, ex.local, if (rare) exR.local else null)
            localScores[w] = out
            out
        }
        val lcS = localContextScores[h.current] ?: kotlin.run {
            val out = getHistories(tags, h, ex.localContext, if (rare) exR.localContext else null)
            localContextScores[h.current] = out
            pairwiseAddInPlace(out, lS)
            out
        }
        val totalS = getHistories(tags, h, ex.dynamic, if (rare) exR.dynamic else null)
        pairwiseAddInPlace(totalS, lcS)
        return totalS
    }

    private fun getHistories(
            tags: Array<String?>,
            h: History,
            extractors: MutableMap<Int, PureExtractor>?,
            extractorsRare: MutableMap<Int, PureExtractor>?
    ): DoubleArray {

        return if (maxentTagger.defaultScore > 0) {
            getApproximateHistories(tags, h, extractors, extractorsRare)
        } else {
            getExactHistories(h, extractors, extractorsRare)
        }
    }

    private fun getExactHistories(
            h: History,
            extractors: MutableMap<Int, PureExtractor>?,
            extractorsRare: MutableMap<Int, PureExtractor>?
    ): DoubleArray {
        val scores = DoubleArray(maxentTagger.ySize)
        val szCommon = maxentTagger.extractors.size
        for ((kf, ex) in extractors!!) {
            for (i in 0 until maxentTagger.ySize) {
                val tag = maxentTagger.tags.getTag(i)
                val fNum = maxentTagger.getNum(FeatureKey(kf, ex.extract(h), tag))
                if (fNum > -1) {
                    scores[i] += maxentTagger.lambdaSolve.lambda[fNum]
                }
            }
        }
        if (extractorsRare != null) {
            for ((kf, ex) in extractorsRare) {
                for (i in 0 until maxentTagger.ySize) {
                    val tag = maxentTagger.tags.getTag(i)
                    val fNum = maxentTagger.getNum(FeatureKey(szCommon + kf, ex.extract(h), tag))
                    if (fNum > -1) {
                        scores[i] += maxentTagger.lambdaSolve.lambda[fNum]
                    }
                }
            }
        }
        return scores
    }

    // Returns an unnormalized score (in log space) for each tag
    private fun getApproximateHistories(
            tags: Array<String?>,
            h: History,
            extractors: MutableMap<Int, PureExtractor>?,
            extractorsRare: MutableMap<Int, PureExtractor>?
    ): DoubleArray {

        val scores = DoubleArray(tags.size)
        val szCommon = maxentTagger.extractors.size
        for ((kf, ex) in extractors!!) {
            for (j in tags.indices) {
                val tag = tags[j]
                val fNum = maxentTagger.getNum(FeatureKey(kf, ex.extract(h), tag))
                if (fNum > -1) {
                    scores[j] += maxentTagger.lambdaSolve.lambda[fNum]
                }
            }
        }
        if (extractorsRare != null) {
            for ((kf, ex) in extractorsRare) {
                for (j in tags.indices) {
                    val tag = tags[j]
                    val fNum = maxentTagger.getNum(FeatureKey(szCommon + kf, ex.extract(h), tag))
                    if (fNum > -1) {
                        scores[j] += maxentTagger.lambdaSolve.lambda[fNum]
                    }
                }
            }
        }
        return scores
    }

    fun leftWindow() = maxentTagger.leftContext //hard-code for now

    fun rightWindow() = maxentTagger.rightContext //hard code for now

    fun getPossibleValues(pos: Int, data: SequenceData): IntArray {
        val arr1 = stringTagsAt(pos, data)!!
        return arr1.indices.map { maxentTagger.tags.getIndex(arr1[it]) }.toIntArray()
    }

    fun scoresOf(tags: IntArray, pos: Int, data: SequenceData): DoubleArray {

        data.history.init(0, data.size - 1, data.size - data.size + pos - leftWindow())
        setHistory(pos, tags, data)
        return getScores(data)
    }

    private fun stringTagsAt(pos: Int, data: SequenceData): Array<String?>? {
        return if (pos < leftWindow() || pos >= data.size + leftWindow()) {
            arrayOf(naTag)
        } else if (maxentTagger.dict.isUnknown(data.sentence[pos - leftWindow()])) {
            maxentTagger.tags.getOpenTags()!!.toTypedArray().also {
                append(it, data.sentence[pos - leftWindow()])
            }
        } else {
            maxentTagger.dict.getTags(data.sentence[pos - leftWindow()]).also {
                append(it!!, data.sentence[pos - leftWindow()])
            }
        }
    }

    companion object {
        private const val eosWord = "EOS"
        private const val naTag = "NA"
    }
}