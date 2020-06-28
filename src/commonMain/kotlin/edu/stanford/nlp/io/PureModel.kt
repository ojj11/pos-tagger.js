package edu.stanford.nlp.io

import edu.stanford.nlp.tagger.maxent.*

@kotlinx.serialization.Serializable
data class PureModel(
        val ySize: Int,
        val extractors: PureExtractors,
        val extractorsRare: PureExtractors,
        val fAssociations: Map<FeatureKey, Int>,
        val lambdaSolve: LambdaSolveTagger,
        val dict: Dictionary,
        val tags: List<String>,
        val closedClassTags: List<String>
)