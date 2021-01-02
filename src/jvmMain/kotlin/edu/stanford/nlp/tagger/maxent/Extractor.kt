package edu.stanford.nlp.tagger.maxent

import java.io.Serializable

/**
 * This class serves as the base class for classes which extract relevant
 * information from a history to give it to the features. Every feature has
 * an associated extractor or maybe more.  GlobalHolder keeps all the
 * extractors; two histories are considered equal if all extractors return
 * equal values for them.  The main functionality of the Extractors is
 * provided by the method extract which takes a History as an argument.
 * The Extractor looks at the history and takes out something important for
 * the features - e.g. specific words and tags at specific positions or
 * some function of the History. The histories are effectively vectors
 * of values, with each dimension being the output of some extractor.
 *
 * When creating a new Extractor subclass, make sure to override the
 * setGlobalHandler method if you need information from the tagger.
 * The best policy is to declare any such data you take from the
 * extractor as "transient", especially if it is a large object such
 * as the dictionary.
 *
 * New extractors are created in either ExtractorFrames or
 * ExtractorFramesRare; those are the places you want to consider
 * adding your new extractor.
 *
 * Note that some extractors can be reused across multiple taggers,
 * but many cannot.  Any extractor that uses information from the
 * tagger such as its dictionary, for example, cannot.  For the
 * moment, some of the extractors in ExtractorFrames and
 * ExtractorFramesRare are static; those are all reusable at the
 * moment, but if you change them in any way to make them not
 * reusable, make sure to change the way they are constructed as well.
 *
 * @author Kristina Toutanova
 * @version 1.0
 */
open class Extractor(
    open val position: Int = Int.MAX_VALUE,
    val isTag: Boolean = false
) : Serializable {

    companion object {
        private const val serialVersionUID = -4694133872973560083L
    }
}
