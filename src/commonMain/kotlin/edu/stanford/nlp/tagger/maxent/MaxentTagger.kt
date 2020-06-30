package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.io.PureParameters
import edu.stanford.nlp.ling.HasWord
import edu.stanford.nlp.ling.TaggedWord
import kotlin.math.max

class MaxentTagger(parameters: PureParameters) {

    val dict: Dictionary
    val tags: TTags
    val extractors: PureExtractors
    val extractorsRare: PureExtractors
    val lambdaSolve: LambdaSolveTagger

    var defaultScore = 0.0
    var leftContext = 0
    var rightContext = 0
    var ySize = 0

    private val fAssociations: Map<FeatureKey, Int>
    private val rareWordThresh: Int

    fun getNum(s: FeatureKey) = fAssociations[s] ?: -1

    private fun setExtractorsGlobal() {
        extractors.setGlobalHolder(this)
        extractorsRare.setGlobalHolder(this)
    }

    fun isRare(word: String?) = dict.sum(word) < rareWordThresh

    /**
     * Returns a new Sentence that is a copy of the given sentence with all the
     * words tagged with their part-of-speech. Convenience method when you only
     * want to tag a single Sentence instead of a Document of sentences.
     * @param sentence sentence to tag
     * @return tagged sentence
     */
    fun tagSentence(sentence: List<HasWord>): List<TaggedWord> {
        val testSentence = TestSentence(this)
        return testSentence.tagSentence(sentence)
    }

    companion object {
        const val RARE_WORD_THRESH = 5
        const val MIN_FEATURE_THRESH = 5
        const val CUR_WORD_MIN_FEATURE_THRESH = 2
        const val RARE_WORD_MIN_FEATURE_THRESH = 10
        const val VERY_COMMON_WORD_THRESH = 250
        const val OCCURRING_TAGS_ONLY = false
        const val POSSIBLE_TAGS_ONLY = false
    }

    init {
        val config = parameters.config
        val lang: String = config.lang
        val openClassTags = config.openClassTags
        val closedClassTags = config.closedClassTags
        rareWordThresh = config.rareWordThresh
        defaultScore = if (lang == "english") 1.0 else 0.0
        if (config.defaultScore >= 0) defaultScore = config.defaultScore

        if (openClassTags!!.isNotEmpty() && lang != "" || closedClassTags!!.isNotEmpty() && lang != "" || closedClassTags.isNotEmpty() && openClassTags.isNotEmpty()) {
            throw RuntimeException("At least two of lang (\"" + lang + "\"), openClassTags (length " + openClassTags.size + ": " + openClassTags.contentToString() + ")," +
                    "and closedClassTags (length " + closedClassTags!!.size + ": " + closedClassTags.contentToString() + ") specified---you must choose one!")
        }

        tags = when {
            openClassTags.isNotEmpty() -> {
                TTags(openClassTags = openClassTags.filterNotNull().toMutableSet())
            }
            closedClassTags.isNotEmpty() -> {
                TTags(closedClassTags = closedClassTags.filterNotNull().toMutableSet())
            }
            else -> {
                TTags(lang)
            }
        }

        val model = parameters.model
        ySize = model.ySize
        extractors = model.extractors
        extractorsRare = model.extractorsRare
        fAssociations = model.fAssociations
        lambdaSolve = model.lambdaSolve
        dict = model.dict
        tags.merge(model.tags, model.closedClassTags)

        extractors.initTypes()
        extractorsRare.initTypes()

        leftContext = max(extractors.leftContext(), extractorsRare.leftContext())
        rightContext = max(extractors.rightContext(), extractorsRare.rightContext())
        setExtractorsGlobal()
    }
}