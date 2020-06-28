package edu.stanford.nlp.io

@kotlinx.serialization.Serializable
data class PureParameters(
        val model: PureModel,
        val config: PureTaggerConfig
)