package com.github.ojj11

class ExactBestSequenceFinder {

    fun bestSequence(data: SequenceData, ts: TestSentence): IntArray {
        val size = data.sentence.size
        val leftWindow = ts.leftWindow()
        val rightWindow = ts.rightWindow()
        val padLength = size + leftWindow + rightWindow
        val tags = (0 until padLength).map { ts.getPossibleValues(it, data) }.toTypedArray()
        val tagNum = (0 until padLength).map { tags[it].size }.toIntArray()
        val tempTags = IntArray(padLength)
        val productSizes = IntArray(padLength)
        var curProduct = 1
        for (i in 0 until leftWindow + rightWindow) {
            curProduct *= tagNum[i]
        }
        for (pos in leftWindow + rightWindow until padLength) {
            if (pos > leftWindow + rightWindow) {
                curProduct /= tagNum[pos - leftWindow - rightWindow - 1] // shift off
            }
            curProduct *= tagNum[pos] // shift on
            productSizes[pos - rightWindow] = curProduct
        }
        val windowScore = (0 until padLength).map { DoubleArray(productSizes[it]) }.toTypedArray()
        for (pos in leftWindow until leftWindow + size) {
            tempTags.fill(tags[0][0])
            for (product in 0 until productSizes[pos]) {
                var p = product
                var shift = 1
                for (curPos in pos + rightWindow downTo pos - leftWindow) {
                    tempTags[curPos] = tags[curPos][p % tagNum[curPos]]
                    p /= tagNum[curPos]
                    if (curPos > pos) {
                        shift *= tagNum[curPos]
                    }
                }

                if (tempTags[pos] == tags[pos][0]) {
                    // get all tags at once
                    val scores = ts.scoresOf(tempTags, pos, data)
                    // fill in the relevant windowScores
                    for (t in 0 until tagNum[pos]) {
                        windowScore[pos][product + t * shift] = scores[t]
                    }
                }
            }
        }
        val score = (0 until padLength).map { DoubleArray(productSizes[it]) }
        val trace = (0 until padLength).map { IntArray(productSizes[it]) }
        for (pos in leftWindow until size + leftWindow) {
            // loop over window product types
            for (product in 0 until productSizes[pos]) {
                // check for initial spot
                if (pos == leftWindow) {
                    // no predecessor type
                    score[pos][product] = windowScore[pos][product]
                    trace[pos][product] = -1
                } else {
                    // loop over possible predecessor types
                    score[pos][product] = Double.NEGATIVE_INFINITY
                    trace[pos][product] = -1
                    val sharedProduct = product / tagNum[pos + rightWindow]
                    val factor = productSizes[pos] / tagNum[pos + rightWindow]
                    for (newTagNum in 0 until tagNum[pos - leftWindow - 1]) {
                        val predProduct = newTagNum * factor + sharedProduct
                        val predScore = score[pos - 1][predProduct] + windowScore[pos][product]
                        if (predScore > score[pos][product]) {
                            score[pos][product] = predScore
                            trace[pos][product] = predProduct
                        }
                    }
                }
            }
        }
        var bestFinalScore = Double.NEGATIVE_INFINITY
        var bestCurrentProduct = -1
        for (product in 0 until productSizes[leftWindow + size - 1]) {
            if (score[leftWindow + size - 1][product] > bestFinalScore) {
                bestCurrentProduct = product
                bestFinalScore = score[leftWindow + size - 1][product]
            }
        }
        var lastProduct = bestCurrentProduct
        var last = padLength - 1
        while (last >= size - 1 && last >= 0) {
            tempTags[last] = tags[last][lastProduct % tagNum[last]]
            lastProduct /= tagNum[last]
            last--
        }
        for (pos in leftWindow + size - 2 downTo leftWindow) {
            val bestNextProduct = bestCurrentProduct
            bestCurrentProduct = trace[pos + 1][bestNextProduct]
            tempTags[pos - leftWindow] = tags[pos - leftWindow][bestCurrentProduct / (productSizes[pos] / tagNum[pos - leftWindow])]
        }
        return tempTags
    }

}