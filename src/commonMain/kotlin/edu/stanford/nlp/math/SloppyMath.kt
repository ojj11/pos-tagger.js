package edu.stanford.nlp.math

/**
 * The class `SloppyMath` contains methods for performing basic
 * numeric operations.  In some cases, such as max and min, they cut a few
 * corners in
 * the implementation for the sake of efficiency.  In particular, they may
 * not handle special notions like NaN and -0.0 correctly.  This was the
 * origin of the class name, but many other methods are just useful
 * math additions, such as logSum.  This class just has static math methds.
 *
 * @author Christopher Manning
 * @version 2003/01/02
 */
object SloppyMath {
    /**
     * If a difference is bigger than this in log terms, then the sum or
     * difference of them will just be the larger (to 12 or so decimal
     * places for double, and 7 or 8 for float).
     */
    const val LOGTOLERANCE = 30.0
}