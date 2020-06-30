package edu.stanford.nlp.process

import edu.stanford.nlp.util.Character

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

    fun lookupShaper(name: String?): Int {
        return when (name?.toLowerCase()) {
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

    private fun dontUseLC(shape: Int): Boolean {
        return shape == WORDSHAPEDAN2 || shape == WORDSHAPEDAN2BIO || shape == WORDSHAPEJENNY1 || shape == WORDSHAPECHRIS2 || shape == WORDSHAPECHRIS3
    }

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
        return sb.toString()
    }

    private const val BOUNDARY_SIZE = 2

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
                    i += gr.length - 1
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
        return sb.toString()
    }

    private fun wordShapeChris2Long(s: String, omitIfInBoundary: Boolean, len: Int, knownLCWords: Collection<String?>?): String {
        val beginChars = CharArray(BOUNDARY_SIZE)
        val endChars = CharArray(BOUNDARY_SIZE)
        var beginUpto = 0
        var endUpto = 0
        val seenSet: MutableSet<Char> = mutableSetOf()
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
        return sb.toString()
    }

    private fun chris4equivalenceClass(c: Char): Char {
        val type = Character.getType(c)
        return if (Character.isDigit(c) || type == Character.LETTER_NUMBER || type == Character.OTHER_NUMBER || "一二三四五六七八九十零〇百千万亿兩○◯".indexOf(c) > 0) {
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
        } else if (type == Character.OTHER_LETTER) {
            'c' // Chinese characters, etc. without case
        } else if (type == Character.CURRENCY_SYMBOL) {
            '$'
        } else if (type == Character.MATH_SYMBOL) {
            '+'
        } else if (type == Character.OTHER_SYMBOL || c == '|') {
            '|'
        } else if (type == Character.START_PUNCTUATION) {
            '('
        } else if (type == Character.END_PUNCTUATION) {
            ')'
        } else if (type == Character.INITIAL_QUOTE_PUNCTUATION) {
            '`'
        } else if (type == Character.FINAL_QUOTE_PUNCTUATION || c == '\'') {
            '\''
        } else if (c == '%') {
            '%'
        } else if (type == Character.OTHER_PUNCTUATION) {
            '.'
        } else if (type == Character.CONNECTOR_PUNCTUATION) {
            '_'
        } else if (type == Character.DASH_PUNCTUATION) {
            '-'
        } else {
            'q'
        }
    }

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

    private fun wordShapeDan2Bio(s: String, knownLCWords: Collection<String?>?): String {
        return if (containsGreekLetter(s)) {
            wordShapeDan2(s, knownLCWords) + "-GREEK"
        } else {
            wordShapeDan2(s, knownLCWords)
        }
    }

    private val greek = arrayOf("alpha", "beta", "gamma", "delta", "epsilon", "zeta", "theta", "iota", "kappa", "lambda", "omicron", "rho", "sigma", "tau", "upsilon", "omega")
    private val biogreek = Regex("alpha|beta|gamma|delta|epsilon|zeta|theta|iota|kappa|lambda|omicron|rho|sigma|tau|upsilon|omega")

    private fun containsGreekLetter(s: String): Boolean {
        return biogreek.matches(s)
    }

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
        return when {
            length == 2 && initCap && period -> "ACRONYM1"
            seenUpper && allCaps && !seenDigit && period -> "ACRONYM"
            seenDigit && dash && !seenUpper && !seenLower -> "DIGIT-DASH"
            initCap && seenLower && seenDigit && dash -> "CAPITALIZED-DIGIT-DASH"
            initCap && seenLower && seenDigit -> "CAPITALIZED-DIGIT"
            initCap && seenLower and dash -> "CAPITALIZED-DASH"
            initCap && seenLower -> "CAPITALIZED"
            seenUpper && allCaps && seenDigit && dash -> "ALLCAPS-DIGIT-DASH"
            seenUpper && allCaps && seenDigit -> "ALLCAPS-DIGIT"
            seenUpper && allCaps && dash -> "ALLCAPS"
            seenUpper && allCaps -> "ALLCAPS"
            seenLower && allLower && seenDigit && dash -> "LOWERCASE-DIGIT-DASH"
            seenLower && allLower && seenDigit -> "LOWERCASE-DIGIT"
            seenLower && allLower && dash -> "LOWERCASE-DASH"
            seenLower && allLower -> "LOWERCASE"
            seenLower && seenDigit -> "MIXEDCASE-DIGIT"
            seenLower -> "MIXEDCASE"
            seenDigit -> "SYMBOL-DIGIT"
            else -> "SYMBOL"
        }
    }
}