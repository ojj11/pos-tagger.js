package edu.stanford.nlp.io

import kotlinx.serialization.Serializable

@Serializable
data class PureParameters(
        val model: PureModel,
        val config: PureTaggerConfig
)