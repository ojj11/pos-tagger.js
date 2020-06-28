package edu.stanford.nlp.tagger.maxent

import java.io.File

/**
 * Extractor for adding distsim information.
 *
 * @author rafferty
 */
@Suppress("unused")
class ExtractorDistsim internal constructor(distSimPath: String, position: Int) : Extractor(position, false) {
    init {
        throw IllegalStateException("Not supported")
    }

    @Suppress("unused")
    class ExtractorDistsimConjunction internal constructor(distSimPath: String, private val left: Int, private val right: Int) : Extractor() {
        init {
            throw IllegalStateException("Not supported")
        }

        companion object {
            private const val serialVersionUID = 1L
        }

    } // end static class ExtractorDistsimConjunction

    companion object {
        private const val serialVersionUID = 1L

        // avoid loading the same lexicon twice but allow different lexicons
        private val lexiconMap: MutableMap<String, Map<String, String>> = HashMap()
    }
}