package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.tagger.maxent.Extractor

/**
 * The word in lower-cased version.
 */
@Suppress("unused")
internal class ExtractorWordLowerCase(override val position: Int) : Extractor(position, false) {
    companion object {
        private const val serialVersionUID = -7847524200422095441L
    }
}

/**
 * This extractor extracts two consecutive words in conjunction,
 * namely leftPosition and leftPosition+1.
 */
@Suppress("unused")
internal class ExtractorTwoWords(val leftPosition: Int) : Extractor(leftPosition, false) {
    companion object {
        private const val serialVersionUID = -1034112287022504917L
    }
}

/**
 * This extractor extracts the current and the next word in conjunction.
 */
@Suppress("unused")
internal class ExtractorCWordNextWord : Extractor() {
    companion object {
        private const val serialVersionUID = -1034112287022504917L
    }
}

/**
 * This extractor extracts the current and the previous word in conjunction.
 */
@Suppress("unused")
internal class ExtractorCWordPrevWord : Extractor() {
    companion object {
        private const val serialVersionUID = -6505213465359458926L
    }
}

/**
 * This extractor extracts the previous two tags.
 */
@Suppress("unused")
internal class ExtractorPrevTwoTags : Extractor() {
    companion object {
        private const val serialVersionUID = 5124896556547424355L
    }
}

/**
 * This extractor extracts the previous three tags.
 */
@Suppress("unused")
internal class ExtractorPrevThreeTags : Extractor() {
    companion object {
        private const val serialVersionUID = 2123985878223958420L
    }
}

/**
 * This extractor extracts the next two tags.
 */
@Suppress("unused")
internal class ExtractorNextTwoTags : Extractor() {
    companion object {
        private const val serialVersionUID = -2623988469984672798L
    }
}

/**
 * This extractor extracts the previous tag and the current word in conjunction.
 */
@Suppress("unused")
internal class ExtractorPrevTagWord : Extractor() {
    companion object {
        private const val serialVersionUID = 1283543246845193024L
    }
}

/**
 * This extractor extracts the previous tag , next tag in conjunction.
 */
@Suppress("unused")
internal class ExtractorPrevTagNextTag : Extractor() {
    companion object {
        private const val serialVersionUID = -2807770765588266257L
    }
}

/**
 * This extractor extracts the next tag and the current word in conjunction.
 */
@Suppress("unused")
internal class ExtractorNextTagWord : Extractor() {
    companion object {
        private const val serialVersionUID = 4037838593446895680L
    }
}