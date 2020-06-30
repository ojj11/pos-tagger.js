package edu.stanford.nlp.util

class BiList<V>(
        private val backingList: MutableList<V> = mutableListOf()
) : MutableList<V> by backingList {

    private var lookupMap = hashMapOf<V, Int>()

    override fun add(element: V): Boolean {
        lookupMap[element] = backingList.size
        return backingList.add(element)
    }

    override fun indexOf(element: V) = lookupMap[element] ?: -1
}