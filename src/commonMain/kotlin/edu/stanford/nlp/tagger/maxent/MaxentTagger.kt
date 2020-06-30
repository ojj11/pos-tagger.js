package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.io.PureParameters
import edu.stanford.nlp.ling.TaggedWord
import edu.stanford.nlp.ling.Word
import kotlin.math.max

class MaxentTagger(parameters: PureParameters) {

    private val features = parameters.model.features
    private val rareWordThresh = parameters.config.rareWordThresh

    val dict = parameters.model.dict
    val tags: TTags
    val extractors = parameters.model.extractors
    val extractorsRare = parameters.model.extractorsRare
    val lambda = parameters.model.lambda
    val leftContext = max(extractors.leftContext(), extractorsRare.leftContext())
    val rightContext = max(extractors.rightContext(), extractorsRare.rightContext())
    val ySize = parameters.model.ySize

    var defaultScore = 0.0

    fun getFeature(s: Feature) = features[s] ?: -1

    fun isRare(word: String) = dict.sum(word) < rareWordThresh

    fun tagSentence(sentence: List<Word>): List<TaggedWord> {
        val testSentence = TestSentence(this)
        return testSentence.tagSentence(sentence)
    }

    init {
        val lang: String = parameters.config.lang
        val openClassTags = parameters.config.openClassTags
        val closedClassTags = parameters.config.closedClassTags

        defaultScore = if (lang == "english") 1.0 else 0.0
        if (parameters.config.defaultScore >= 0) defaultScore = parameters.config.defaultScore

        if (openClassTags.isNotEmpty() && lang != "" || closedClassTags.isNotEmpty() && lang != "" || closedClassTags.isNotEmpty() && openClassTags.isNotEmpty()) {
            throw RuntimeException("At least two of lang (\"" + lang + "\"), openClassTags (length " + openClassTags.size + ": " + openClassTags.contentToString() + ")," +
                    "and closedClassTags (length " + closedClassTags.size + ": " + closedClassTags.contentToString() + ") specified---you must choose one!")
        }

        tags = when {
            openClassTags.isNotEmpty() -> {
                TTags(openClassTags = openClassTags.toMutableSet())
            }
            closedClassTags.isNotEmpty() -> {
                TTags(closedClassTags = closedClassTags.toMutableSet())
            }
            else -> {
                TTags(lang)
            }
        }

        tags.merge(parameters.model.tags, parameters.model.closedClassTags)

        extractors.setGlobalHolder(this)
        extractorsRare.setGlobalHolder(this)
    }
}