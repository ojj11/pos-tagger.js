package edu.stanford.nlp.tagger.maxent

/**
 * This module does the working out of lambda parameters for binary tagger
 * features.  It can use either IIS or CG.
 *
 * @author Kristina Toutanova
 * @version 1.0
 */
@kotlinx.serialization.Serializable
class LambdaSolveTagger(
        val lambda: DoubleArray
)