package edu.stanford.nlp.tagger.maxent

import java.io.Serializable

/** Maintains a set of feature extractors and applies them.
 *
 * @author Kristina Toutanova
 * @version 1.0
 */
class Extractors(extrs: Array<Extractor>) : Serializable {
    val v: Array<Extractor> = extrs.copyOf()

    companion object {
        private const val serialVersionUID = -4777107742414749890L
    }
}
