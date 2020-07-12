package edu.stanford.nlp.util

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

private const val PROP = "prop"
private const val PROPS = "props"
private const val PROPERTIES = "properties"

/**
 * StringUtils is a class for random String things, including output
 * formatting and command line argument parsing.
 *
 * @author Dan Klein
 * @author Christopher Manning
 * @author Tim Grow (grow@stanford.edu)
 * @author Chris Cox
 * @version 2006/02/03
 */
object StringUtils {
    val EMPTY_STRING_ARRAY = arrayOf<String>()

    /**
     * In this version each flag has zero or one argument. It has one argument
     * if there is a thing following a flag that does not begin with '-'.  See
     * [.argsToProperties] for full documentation.
     *
     * @param args Command line arguments
     * @return A Properties object representing the arguments.
     */
    fun argsToProperties(args: Array<String>, flagsToNumArgs: Map<String?, Int?> = emptyMap<String?, Int>()): Properties {
        val result = Properties()
        val remainingArgs: MutableList<String> = ArrayList()
        var i = 0
        while (i < args.size) {
            var key = args[i]
            if (key.isNotEmpty() && key[0] == '-') { // found a flag
                key = key.substring(1) // strip off the hyphen
                val maxFlagArgs = flagsToNumArgs[key]
                val max = maxFlagArgs ?: 1
                val min = maxFlagArgs ?: 0
                val flagArgs: MutableList<String> = ArrayList()
                // cdm oct 2007: add length check to allow for empty string argument!
                var j = 0
                while (j < max && i + 1 < args.size && (j < min || args[i + 1].isEmpty() || args[i + 1].isNotEmpty() && args[i + 1][0] != '-')) {
                    flagArgs.add(args[i + 1])
                    i++
                    j++
                }
                if (flagArgs.isEmpty()) {
                    result.setProperty(key, "true")
                } else {
                    result.setProperty(key, flagArgs.joinToString(separator = " "))
                    if (key.equals(PROP, ignoreCase = true) || key.equals(PROPS, ignoreCase = true) || key.equals(PROPERTIES, ignoreCase = true)) {
                        try {
                            val `is`: InputStream = BufferedInputStream(FileInputStream(result.getProperty(key)))
                            result.remove(key) // location of this line is critical
                            result.load(`is`)
                            // trim all values
                            for (propKey in result.keys) {
                                val newVal = result.getProperty(propKey as String)
                                result.setProperty(propKey, newVal.trim { it <= ' ' })
                            }
                            `is`.close()
                        } catch (e: IOException) {
                            result.remove(key)
                            throw RuntimeException(e)
                        }
                    }
                }
            } else {
                remainingArgs.add(args[i])
            }
            i++
        }
        if (remainingArgs.isNotEmpty()) {
            result.setProperty("", remainingArgs.joinToString(separator = " "))
        }
        if (result.containsKey(PROP)) {
            val file = result.getProperty(PROP)
            result.remove(PROP)
            val toAdd = argsToProperties(arrayOf("-prop", file))
            val e = toAdd.propertyNames()
            while (e.hasMoreElements()) {
                val key = e.nextElement() as String
                val `val` = toAdd.getProperty(key)
                if (!result.containsKey(key)) {
                    result.setProperty(key, `val`)
                }
            }
        }
        return result
    }
}