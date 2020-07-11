package com.github.ojj11

import kotlin.math.max

class MaxentTagger(parameters: PureParameters) {

    val features = parameters.features
    val rareWordThresh = parameters.rareWordThresh
    val dict = parameters.dict
    val tags = parameters.tags
    val extractors = parameters.extractors
    val extractorsRare = parameters.extractorsRare
    val lambda = parameters.lambda
    val leftContext = max(extractors.leftContext(), extractorsRare.leftContext())
    val rightContext = max(extractors.rightContext(), extractorsRare.rightContext())
    val ySize = parameters.ySize

    var defaultScore = 0.0

    fun tagSentence(sentence: Array<String>): Array<TaggedWord> {
        val testSentence = TestSentence(this)
        return testSentence.tagSentence(sentence)
    }

    init {
        val lang: String = parameters.lang

        defaultScore = if (lang == "english") 1.0 else 0.0
        if (parameters.defaultScore >= 0) defaultScore = parameters.defaultScore

        extractors.setGlobalHolder(this)
        extractorsRare.setGlobalHolder(this)
    }
}