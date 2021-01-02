package edu.stanford.nlp.tagger.maxent

/**
 * This class is the same as a regular Extractor, but keeps a pointer
 * to the tagger's dictionary as well.
 *
 * Obviously that means this kind of extractor is not reusable across
 * multiple taggers (see comments Extractor.java), so no extractor of
 * this type should be declared static.
 */
open class DictionaryExtractor : Extractor() {
    companion object {
        private const val serialVersionUID = 692763177746328195L
    }
}
