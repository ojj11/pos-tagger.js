package edu.stanford.nlp.io

@kotlinx.serialization.Serializable
data class PureTaggerConfig(
        val defaultScore: Double,
        val learnClosedClassTags: Boolean,
        val closedClassTags: Array<String?>?,
        val openClassTags: Array<String?>?,
        val lang: String,
        val rareWordThresh: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PureTaggerConfig

        if (defaultScore != other.defaultScore) return false
        if (learnClosedClassTags != other.learnClosedClassTags) return false
        if (closedClassTags != null) {
            if (other.closedClassTags == null) return false
            if (!closedClassTags.contentEquals(other.closedClassTags)) return false
        } else if (other.closedClassTags != null) return false
        if (openClassTags != null) {
            if (other.openClassTags == null) return false
            if (!openClassTags.contentEquals(other.openClassTags)) return false
        } else if (other.openClassTags != null) return false
        if (lang != other.lang) return false
        if (rareWordThresh != other.rareWordThresh) return false

        return true
    }

    override fun hashCode(): Int {
        var result = defaultScore.hashCode()
        result = 31 * result + learnClosedClassTags.hashCode()
        result = 31 * result + (closedClassTags?.contentHashCode() ?: 0)
        result = 31 * result + (openClassTags?.contentHashCode() ?: 0)
        result = 31 * result + lang.hashCode()
        result = 31 * result + rareWordThresh
        return result
    }
}