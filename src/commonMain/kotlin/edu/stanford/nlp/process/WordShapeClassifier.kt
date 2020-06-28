package edu.stanford.nlp.process

import edu.stanford.nlp.util.Character

/**
 * Provides static methods which
 * map any String to another String indicative of its "word shape" -- e.g.,
 * whether capitalized, numeric, etc.  Different implementations may
 * implement quite different, normally language specific ideas of what
 * word shapes are useful.
 *
 * @author Christopher Manning
 * @author Dan Klein
 */
object WordShapeClassifier {
    private const val NOWORDSHAPE = -1
    private const val WORDSHAPEDAN1 = 0
    private const val WORDSHAPECHRIS1 = 1
    private const val WORDSHAPEDAN2 = 2
    private const val WORDSHAPEDAN2USELC = 3
    private const val WORDSHAPEDAN2BIO = 4
    private const val WORDSHAPEDAN2BIOUSELC = 5
    private const val WORDSHAPEJENNY1 = 6
    private const val WORDSHAPEJENNY1USELC = 7
    private const val WORDSHAPECHRIS2 = 8
    private const val WORDSHAPECHRIS2USELC = 9
    private const val WORDSHAPECHRIS3 = 10
    private const val WORDSHAPECHRIS3USELC = 11
    private const val WORDSHAPECHRIS4 = 12

    /** Look up a shaper by a short String name.
     *
     * @param name Shaper name.  Known names have patterns along the lines of:
     * dan[12](bio)?(UseLC)?, jenny1(useLC)?, chris[1234](useLC)?.
     * @return An integer constant for the shaper
     */
    fun lookupShaper(name: String?): Int {
        return when(name?.toLowerCase()) {
            null -> NOWORDSHAPE
            "dan1" -> WORDSHAPEDAN1
            "chris1" -> WORDSHAPECHRIS1
            "dan2" -> WORDSHAPEDAN2
            "dan2useLC" -> WORDSHAPEDAN2USELC
            "dan2bio" -> WORDSHAPEDAN2BIO
            "dan2bioUseLC" -> WORDSHAPEDAN2BIOUSELC
            "jenny1" -> WORDSHAPEJENNY1
            "jenny1useLC" -> WORDSHAPEJENNY1USELC
            "chris2" -> WORDSHAPECHRIS2
            "chris2useLC" -> WORDSHAPECHRIS2USELC
            "chris3" -> WORDSHAPECHRIS3
            "chris3useLC" -> WORDSHAPECHRIS3USELC
            "chris4" -> WORDSHAPECHRIS4
            else -> NOWORDSHAPE
        }
    }

    /**
     * Returns true if the specified word shaper doesn't use
     * known lower case words, even if a list of them is present.
     * This is used for backwards compatibility. It is suggested that
     * new word shape functions are either passed a non-null list of
     * lowercase words or not, depending on whether you want knownLC marking
     * (if it is available in a shaper).  This is how chris4 works.
     *
     * @param shape One of the defined shape constants
     * @return true if the specified word shaper uses
     * known lower case words.
     */
    private fun dontUseLC(shape: Int): Boolean {
        return shape == WORDSHAPEDAN2 || shape == WORDSHAPEDAN2BIO || shape == WORDSHAPEJENNY1 || shape == WORDSHAPECHRIS2 || shape == WORDSHAPECHRIS3
    }

    /**
     * Specify the String and the int identifying which word shaper to
     * use and this returns the result of using that wordshaper on the String.
     *
     * @param inStr String to calculate word shpape of
     * @param wordShaper Constant for which shaping formula to use
     * @return The wordshape String
     */
    fun wordShape(inStr: String, wordShaper: Int, knownLCWords: Collection<String?>? = null): String {
        // this first bit is for backwards compatibility with how things were first
        // implemented, where the word shaper name encodes whether to useLC.
        // If the shaper is in the old compatibility list, then a specified
        // list of knownLCwords is ignored
        var knownLCWords = knownLCWords
        if (knownLCWords != null && dontUseLC(wordShaper)) {
            knownLCWords = null
        }
        return when (wordShaper) {
            NOWORDSHAPE -> inStr
            WORDSHAPEDAN1 -> wordShapeDan1(inStr)
            WORDSHAPECHRIS1 -> wordShapeChris1(inStr)
            WORDSHAPEDAN2, WORDSHAPEDAN2USELC -> wordShapeDan2(inStr, knownLCWords)
            WORDSHAPEDAN2BIO, WORDSHAPEDAN2BIOUSELC -> wordShapeDan2Bio(inStr, knownLCWords)
            WORDSHAPEJENNY1, WORDSHAPEJENNY1USELC -> wordShapeJenny1(inStr, knownLCWords)
            WORDSHAPECHRIS2, WORDSHAPECHRIS2USELC -> wordShapeChris2(inStr, false, knownLCWords)
            WORDSHAPECHRIS3, WORDSHAPECHRIS3USELC -> wordShapeChris2(inStr, true, knownLCWords)
            WORDSHAPECHRIS4 -> wordShapeChris4(inStr, knownLCWords)
            else -> throw IllegalStateException("Bad WordShapeClassifier")
        }
    }

    /**
     * A fairly basic 5-way classifier, that notes digits, and upper
     * and lower case, mixed, and non-alphanumeric.
     *
     * @param s String to find word shape of
     * @return Its word shape: a 5 way classification
     */
    private fun wordShapeDan1(s: String): String {
        var digit = true
        var upper = true
        var lower = true
        var mixed = true
        for (i in s.indices) {
            val c = s[i]
            if (!Character.isDigit(c)) {
                digit = false
            }
            if (!Character.isLowerCase(c)) {
                lower = false
            }
            if (!Character.isUpperCase(c)) {
                upper = false
            }
            if (i == 0 && !Character.isUpperCase(c) || i >= 1 && !Character.isLowerCase(c)) {
                mixed = false
            }
        }
        if (digit) {
            return "ALL-DIGITS"
        }
        if (upper) {
            return "ALL-UPPER"
        }
        if (lower) {
            return "ALL-LOWER"
        }
        return if (mixed) {
            "MIXED-CASE"
        } else "OTHER"
    }

    /**
     * A fine-grained word shape classifier, that equivalence classes
     * lower and upper case and digits, and collapses sequences of the
     * same type, but keeps all punctuation, etc.
     *
     *
     * *Note:* We treat '_' as a lowercase letter, sort of like many
     * programming languages.  We do this because we use '_' joining of
     * tokens in some applications like RTE.
     *
     * @param s           The String whose shape is to be returned
     * @param knownLCWords If this is non-null and non-empty, mark words whose
     * lower case form is found in the
     * Collection of known lower case words
     * @return The word shape
     */
    private fun wordShapeDan2(s: String, knownLCWords: Collection<String?>?): String {
        val sb = StringBuilder("WT-")
        var lastM = '~'
        var nonLetters = false
        val len = s.length
        for (i in 0 until len) {
            val c = s[i]
            var m = c
            if (Character.isDigit(c)) {
                m = 'd'
            } else if (Character.isLowerCase(c) || c == '_') {
                m = 'x'
            } else if (Character.isUpperCase(c)) {
                m = 'X'
            }
            if (m != 'x' && m != 'X') {
                nonLetters = true
            }
            if (m != lastM) {
                sb.append(m)
            }
            lastM = m
        }
        if (len <= 3) {
            sb.append(':').append(len)
        }
        if (knownLCWords != null) {
            if (!nonLetters && knownLCWords.contains(s.toLowerCase())) {
                sb.append('k')
            }
        }
        // System.err.println("wordShapeDan2: " + s + " became " + sb);
        return sb.toString()
    }

    private fun wordShapeJenny1(s: String, knownLCWords: Collection<String?>?): String {
        val sb = StringBuilder("WT-")
        var lastM = '~'
        var nonLetters = false
        var i = 0
        while (i < s.length) {
            val c = s[i]
            var m = when {
                Character.isDigit(c) -> 'd'
                Character.isLowerCase(c) -> 'x'
                Character.isUpperCase(c) -> 'X'
                else -> c
            }
            for (gr in greek) {
                if (s.startsWith(gr, i)) {
                    m = 'g'
                    i = i + gr.length - 1
                    //System.out.println(s + "  ::  " + s.substring(i+1));
                    break
                }
            }
            if (m != 'x' && m != 'X') {
                nonLetters = true
            }
            if (m != lastM) {
                sb.append(m)
            }
            lastM = m
            i++
        }
        if (s.length <= 3) {
            sb.append(':').append(s.length)
        }
        if (knownLCWords != null) {
            if (!nonLetters && knownLCWords.contains(s.toLowerCase())) {
                sb.append('k')
            }
        }
        //System.out.println(s+" became "+sb);
        return sb.toString()
    }

    /** Note: the optimizations in wordShapeChris2 would break if BOUNDARY_SIZE
     * was greater than the shortest greek word, so valid values are: 0, 1, 2, 3.
     */
    private const val BOUNDARY_SIZE = 2

    /**
     * This one picks up on Dan2 ideas, but seeks to make less distinctions
     * mid sequence by sorting for long words, but to maintain extra
     * distinctions for short words. It exactly preserves the character shape
     * of the first and last 2 (i.e., BOUNDARY_SIZE) characters and then
     * will record shapes that occur between them (perhaps only if they are
     * different)
     *
     * @param s The String to find the word shape of
     * @param omitIfInBoundary If true, character classes present in the
     * first or last two (i.e., BOUNDARY_SIZE) letters
     * of the word are not also registered
     * as classes that appear in the middle of the word.
     * @param knownLCWords If non-null and non-empty, tag with a "k" suffix words
     * that are in this list when lowercased (representing
     * that the word is "known" as a lowercase word).
     * @return A word shape for the word.
     */
    private fun wordShapeChris2(s: String, omitIfInBoundary: Boolean, knownLCWords: Collection<String?>?): String {
        val len = s.length
        return if (len <= BOUNDARY_SIZE * 2) {
            wordShapeChris2Short(s, len, knownLCWords)
        } else {
            wordShapeChris2Long(s, omitIfInBoundary, len, knownLCWords)
        }
    }

    // Do the simple case of words <= BOUNDARY_SIZE * 2 (i.e., 4) with only 1 object allocation!
    private fun wordShapeChris2Short(s: String, len: Int, knownLCWords: Collection<String?>?): String {
        val sbLen = if (knownLCWords != null) len + 1 else len // markKnownLC makes String 1 longer
        val sb = StringBuilder(sbLen)
        var nonLetters = false
        var i = 0
        while (i < len) {
            val c = s[i]
            var m = c
            if (Character.isDigit(c)) {
                m = 'd'
            } else if (Character.isLowerCase(c)) {
                m = 'x'
            } else if (Character.isUpperCase(c) || Character.isTitleCase(c)) {
                m = 'X'
            }
            for (gr in greek) {
                if (s.startsWith(gr, i)) {
                    m = 'g'
                    //System.out.println(s + "  ::  " + s.substring(i+1));
                    i += gr.length - 1
                    // System.out.println("Position skips to " + i);
                    break
                }
            }
            if (m != 'x' && m != 'X') {
                nonLetters = true
            }
            sb.append(m)
            i++
        }
        if (knownLCWords != null) {
            if (!nonLetters && knownLCWords.contains(s.toLowerCase())) {
                sb.append('k')
            }
        }
        // System.out.println(s + " became " + sb);
        return sb.toString()
    }

    // introduce sizes and optional allocation to reduce memory churn demands;
    // this class could blow a lot of memory if used in a tight loop,
    // as the naive version allocates lots of kind of heavyweight objects
    // endSB should be of length BOUNDARY_SIZE
    // sb is maximally of size s.length() + 1, but is usually (much) shorter. The +1 might happen if markKnownLC is true and it applies
    // boundSet is maximally of size BOUNDARY_SIZE * 2 (and is often smaller)
    // seenSet is maximally of size s.length() - BOUNDARY_SIZE * 2, but might often be of size <= 4. But it has no initial size allocation
    // But we want the initial size to be greater than BOUNDARY_SIZE * 2 * (4/3) since the default loadfactor is 3/4.
    // That is, of size 6, which become 8, since HashMaps are powers of 2.  Still, it's half the size
    private fun wordShapeChris2Long(s: String, omitIfInBoundary: Boolean, len: Int, knownLCWords: Collection<String?>?): String {
        val beginChars = CharArray(BOUNDARY_SIZE)
        val endChars = CharArray(BOUNDARY_SIZE)
        var beginUpto = 0
        var endUpto = 0
        val seenSet: MutableSet<Char> = mutableSetOf() // TreeSet guarantees stable ordering; has no size parameter
        var nonLetters = false
        var i = 0
        while (i < len) {
            var iIncr = 0
            val c = s[i]
            var m = c
            if (Character.isDigit(c)) {
                m = 'd'
            } else if (Character.isLowerCase(c)) {
                m = 'x'
            } else if (Character.isUpperCase(c) || Character.isTitleCase(c)) {
                m = 'X'
            }
            for (gr in greek) {
                if (s.startsWith(gr, i)) {
                    m = 'g'
                    //System.out.println(s + "  ::  " + s.substring(i+1));
                    iIncr = gr.length - 1
                    break
                }
            }
            if (m != 'x' && m != 'X') {
                nonLetters = true
            }
            when {
                i < BOUNDARY_SIZE -> beginChars[beginUpto++] = m
                i < len - BOUNDARY_SIZE -> seenSet.add(m)
                else -> endChars[endUpto++] = m
            }
            i += iIncr
            i++
        }

        // Calculate size. This may be an upperbound, but is often correct
        var sbSize = beginUpto + endUpto + seenSet.size
        if (knownLCWords != null) {
            sbSize++
        }
        val sb = StringBuilder(sbSize)
        // put in the beginning chars
        sb.append(beginChars, 0, beginUpto)
        // put in the stored ones sorted
        if (omitIfInBoundary) {
            for (chr in seenSet) {
                var insert = true
                for (i in 0 until beginUpto) {
                    if (beginChars[i] == chr) {
                        insert = false
                        break
                    }
                }
                for (i in 0 until endUpto) {
                    if (endChars[i] == chr) {
                        insert = false
                        break
                    }
                }
                if (insert) {
                    sb.append(chr)
                }
            }
        } else {
            for (chr in seenSet) {
                sb.append(chr)
            }
        }
        // and add end ones
        sb.append(endChars, 0, endUpto)
        if (knownLCWords != null) {
            if (!nonLetters && knownLCWords.contains(s.toLowerCase())) {
                sb.append('k')
            }
        }
        // System.out.println(s + " became " + sb);
        return sb.toString()
    }

    private fun chris4equivalenceClass(c: Char): Char {
        val type = Character.getType(c)
        return if (Character.isDigit(c) || type == Character.LETTER_NUMBER.toInt() || type == Character.OTHER_NUMBER.toInt() || "一二三四五六七八九十零〇百千万亿兩○◯".indexOf(c) > 0) {
            // include Chinese numbers that are just of unicode type OTHER_LETTER (and a couple of round symbols often used (by mistake?) for zeroes)
            'd'
        } else if (c == '第') {
            'o' // detect those Chinese ordinals!
        } else if (c == '年' || c == '月' || c == '日') { // || c == '号') {
            'D' // Chinese date characters.
        } else if (Character.isLowerCase(c)) {
            'x'
        } else if (Character.isUpperCase(c) || Character.isTitleCase(c)) {
            'X'
        } else if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
            's'
        } else if (type == Character.OTHER_LETTER.toInt()) {
            'c' // Chinese characters, etc. without case
        } else if (type == Character.CURRENCY_SYMBOL.toInt()) {
            '$'
        } else if (type == Character.MATH_SYMBOL.toInt()) {
            '+'
        } else if (type == Character.OTHER_SYMBOL.toInt() || c == '|') {
            '|'
        } else if (type == Character.START_PUNCTUATION.toInt()) {
            '('
        } else if (type == Character.END_PUNCTUATION.toInt()) {
            ')'
        } else if (type == Character.INITIAL_QUOTE_PUNCTUATION.toInt()) {
            '`'
        } else if (type == Character.FINAL_QUOTE_PUNCTUATION.toInt() || c == '\'') {
            '\''
        } else if (c == '%') {
            '%'
        } else if (type == Character.OTHER_PUNCTUATION.toInt()) {
            '.'
        } else if (type == Character.CONNECTOR_PUNCTUATION.toInt()) {
            '_'
        } else if (type == Character.DASH_PUNCTUATION.toInt()) {
            '-'
        } else {
            'q'
        }
    }

    /**
     * This one picks up on Dan2 ideas, but seeks to make less distinctions
     * mid sequence by sorting for long words, but to maintain extra
     * distinctions for short words, by always recording the class of the
     * first and last two characters of the word.
     * Compared to chris2 on which it is based,
     * it uses more Unicode classes, and so collapses things like
     * punctuation more, and might work better with real unicode.
     *
     * @param s The String to find the word shape of
     * @param knownLCWords If non-null and non-empty, tag with a "k" suffix words
     * that are in this list when lowercased (representing
     * that the word is "known" as a lowercase word).
     * @return A word shape for the word.
     */
    private fun wordShapeChris4(s: String, knownLCWords: Collection<String?>?): String {
        val len = s.length
        return if (len <= BOUNDARY_SIZE * 2) {
            wordShapeChris4Short(s, len, knownLCWords)
        } else {
            wordShapeChris4Long(s, len, knownLCWords)
        }
    }

    // Do the simple case of words <= BOUNDARY_SIZE * 2 (i.e., 4) with only 1 object allocation!
    private fun wordShapeChris4Short(s: String, len: Int, knownLCWords: Collection<String?>?): String {
        val sbLen = if (knownLCWords != null) len + 1 else len // markKnownLC makes String 1 longer
        val sb = StringBuilder(sbLen)
        var nonLetters = false
        var i = 0
        while (i < len) {
            val c = s[i]
            var m = chris4equivalenceClass(c)
            for (gr in greek) {
                if (s.startsWith(gr, i)) {
                    m = 'g'
                    //System.out.println(s + "  ::  " + s.substring(i+1));
                    i += gr.length - 1
                    // System.out.println("Position skips to " + i);
                    break
                }
            }
            if (m != 'x' && m != 'X') {
                nonLetters = true
            }
            sb.append(m)
            i++
        }
        if (knownLCWords != null) {
            if (!nonLetters && knownLCWords.contains(s.toLowerCase())) {
                sb.append('k')
            }
        }
        // System.out.println(s + " became " + sb);
        return sb.toString()
    }

    private fun wordShapeChris4Long(s: String, len: Int, knownLCWords: Collection<String?>?): String {
        val sb = StringBuilder(s.length + 1)
        val endSB = StringBuilder(BOUNDARY_SIZE)
        val seenSet: MutableSet<Char> = mutableSetOf() // TreeSet guarantees stable ordering
        var nonLetters = false
        var i = 0
        while (i < len) {
            val c = s[i]
            var m = chris4equivalenceClass(c)
            var iIncr = 0
            for (gr in greek) {
                if (s.startsWith(gr, i)) {
                    m = 'g'
                    iIncr = gr.length - 1
                    //System.out.println(s + "  ::  " + s.substring(i+1));
                    break
                }
            }
            if (m != 'x' && m != 'X') {
                nonLetters = true
            }
            when {
                i < BOUNDARY_SIZE -> sb.append(m)
                i < len - BOUNDARY_SIZE -> seenSet.add(m)
                else -> endSB.append(m)
            }
            // System.out.println("Position " + i + " --> " + m);
            i += iIncr
            i++
        }
        // put in the stored ones sorted and add end ones
        for (chr in seenSet) {
            sb.append(chr)
        }
        sb.append(endSB)
        if (knownLCWords != null) {
            if (!nonLetters && knownLCWords.contains(s.toLowerCase())) {
                sb.append('k')
            }
        }
        // System.out.println(s + " became " + sb);
        return sb.toString()
    }

    /**
     * Returns a fine-grained word shape classifier, that equivalence classes
     * lower and upper case and digits, and collapses sequences of the
     * same type, but keeps all punctuation.  This adds an extra recognizer
     * for a greek letter embedded in the String, which is useful for bio.
     */
    private fun wordShapeDan2Bio(s: String, knownLCWords: Collection<String?>?): String {
        return if (containsGreekLetter(s)) {
            wordShapeDan2(s, knownLCWords) + "-GREEK"
        } else {
            wordShapeDan2(s, knownLCWords)
        }
    }

    /** List of greek letters for bio.  We omit eta, mu, nu, xi, phi, chi, psi.
     * Maybe should omit rho too, but it is used in bio "Rho kinase inhibitor".
     */
    private val greek = arrayOf("alpha", "beta", "gamma", "delta", "epsilon", "zeta", "theta", "iota", "kappa", "lambda", "omicron", "rho", "sigma", "tau", "upsilon", "omega")
    private val biogreek = Regex("alpha|beta|gamma|delta|epsilon|zeta|theta|iota|kappa|lambda|omicron|rho|sigma|tau|upsilon|omega")

    /**
     * Somewhat ad-hoc list of only greek letters that bio people use, partly
     * to avoid false positives on short ones.
     * @param s String to check for Greek
     * @return true iff there is a greek lette embedded somewhere in the String
     */
    private fun containsGreekLetter(s: String): Boolean {
        return biogreek.matches(s)
    }

    /** This one equivalence classes all strings into one of 24 semantically
     * informed classes, somewhat similarly to the function specified in the
     * BBN Nymble NER paper (Bikel et al. 1997).
     *
     *
     * Note that it regards caseless non-Latin letters as lowercase.
     *
     * @param s String to word class
     * @return The string's class
     */
    private fun wordShapeChris1(s: String): String {
        val length = s.length
        if (length == 0) {
            return "SYMBOL" // unclear if this is sensible, but it's what a length 0 String becomes....
        }
        var cardinal = false
        var number = true
        var seenDigit = false
        var seenNonDigit = false
        for (i in 0 until length) {
            val ch = s[i]
            var digit = Character.isDigit(ch)
            if (digit) {
                seenDigit = true
            } else {
                seenNonDigit = true
            }
            // allow commas, decimals, and negative numbers
            digit = digit || ch == '.' || ch == ',' || i == 0 && (ch == '-' || ch == '+')
            if (!digit) {
                number = false
            }
        }
        if (!seenDigit) {
            number = false
        } else if (!seenNonDigit) {
            cardinal = true
        }
        if (cardinal) {
            return when {
                length < 4 -> "CARDINAL13"
                length == 4 -> "CARDINAL4"
                else -> "CARDINAL5PLUS"
            }
        } else if (number) {
            return "NUMBER"
        }
        var seenLower = false
        var seenUpper = false
        var allCaps = true
        var allLower = true
        var initCap = false
        var dash = false
        var period = false
        for (i in 0 until length) {
            val ch = s[i]
            val up = Character.isUpperCase(ch)
            val let = Character.isLetter(ch)
            val tit = Character.isTitleCase(ch)
            if (ch == '-') {
                dash = true
            } else if (ch == '.') {
                period = true
            }
            when {
                tit -> {
                    seenUpper = true
                    allLower = false
                    seenLower = true
                    allCaps = false
                }
                up -> {
                    seenUpper = true
                    allLower = false
                }
                let -> {
                    seenLower = true
                    allCaps = false
                }
            }
            if (i == 0 && (up || tit)) {
                initCap = true
            }
        }
        return if (length == 2 && initCap && period) {
            "ACRONYM1"
        } else if (seenUpper && allCaps && !seenDigit && period) {
            "ACRONYM"
        } else if (seenDigit && dash && !seenUpper && !seenLower) {
            "DIGIT-DASH"
        } else if (initCap && seenLower && seenDigit && dash) {
            "CAPITALIZED-DIGIT-DASH"
        } else if (initCap && seenLower && seenDigit) {
            "CAPITALIZED-DIGIT"
        } else if (initCap && seenLower and dash) {
            "CAPITALIZED-DASH"
        } else if (initCap && seenLower) {
            "CAPITALIZED"
        } else if (seenUpper && allCaps && seenDigit && dash) {
            "ALLCAPS-DIGIT-DASH"
        } else if (seenUpper && allCaps && seenDigit) {
            "ALLCAPS-DIGIT"
        } else if (seenUpper && allCaps && dash) {
            "ALLCAPS"
        } else if (seenUpper && allCaps) {
            "ALLCAPS"
        } else if (seenLower && allLower && seenDigit && dash) {
            "LOWERCASE-DIGIT-DASH"
        } else if (seenLower && allLower && seenDigit) {
            "LOWERCASE-DIGIT"
        } else if (seenLower && allLower && dash) {
            "LOWERCASE-DASH"
        } else if (seenLower && allLower) {
            "LOWERCASE"
        } else if (seenLower && seenDigit) {
            "MIXEDCASE-DIGIT"
        } else if (seenLower) {
            "MIXEDCASE"
        } else if (seenDigit) {
            "SYMBOL-DIGIT"
        } else {
            "SYMBOL"
        }
    }
}