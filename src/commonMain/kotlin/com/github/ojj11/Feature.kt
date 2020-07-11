package com.github.ojj11

import kotlinx.serialization.Serializable

@Serializable
data class Feature(
        private val num: Int,
        private val value: String,
        private val tag: String
)