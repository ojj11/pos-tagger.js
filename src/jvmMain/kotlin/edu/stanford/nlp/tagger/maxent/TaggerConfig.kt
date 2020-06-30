package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.util.StringUtils
import edu.stanford.nlp.util.StringUtils.argsToProperties
import java.io.*
import java.net.URL
import java.util.*
import java.util.zip.GZIPInputStream
import kotlin.collections.HashMap
import kotlin.system.exitProcess

/**
 * Reads and stores configuration information for a POS tagger.
 *
 * *Implementation note:* To add a new parameter: (1) define a default
 * String value, (2) add it to defaultValues hash, (3) add line to constructor,
 * (4) add getter method, (5) add to dump() method, (6) add to printGenProps()
 * method, (7) add to class javadoc of MaxentTagger.
 *
 * @author William Morgan
 * @author Anna Rafferty
 * @author Michel Galley
 */
class TaggerConfig : Properties /* Inherits implementation of serializable! */ {
    private enum class Mode {
        TRAIN, TEST, TAG, CONVERT, DUMP
    }

    private var mode = Mode.TAG
        private set

    companion object {
        private const val serialVersionUID = -4136407850147157497L

        /* defaults. Note: no known property has a null value! Don't need to test. */
        private const val SEARCH = "qn"
        private const val TAG_SEPARATOR = "/"
        private const val TOKENIZE = "true"
        private const val DEBUG = "false"
        private const val ITERATIONS = "100"
        private const val ARCH = ""
        private val RARE_WORD_THRESH: String = MaxentTagger.RARE_WORD_THRESH.toString()
        private val MIN_FEATURE_THRESH: String = MaxentTagger.MIN_FEATURE_THRESH.toString()
        private val CUR_WORD_MIN_FEATURE_THRESH: String = MaxentTagger.CUR_WORD_MIN_FEATURE_THRESH.toString()
        private val RARE_WORD_MIN_FEATURE_THRESH: String = MaxentTagger.RARE_WORD_MIN_FEATURE_THRESH.toString()
        private val VERY_COMMON_WORD_THRESH: String = MaxentTagger.VERY_COMMON_WORD_THRESH.toString()
        private val OCCURING_TAGS_ONLY: String = MaxentTagger.OCCURRING_TAGS_ONLY.toString()
        private val POSSIBLE_TAGS_ONLY: String = MaxentTagger.POSSIBLE_TAGS_ONLY.toString()
        private const val SIGMA_SQUARED = 0.5.toString()
        private const val ENCODING = "UTF-8"
        private const val LEARN_CLOSED_CLASS = "false"
        private val CLOSED_CLASS_THRESHOLD: String = TTags.CLOSED_TAG_THRESHOLD.toString()
        private const val VERBOSE = "false"
        private const val VERBOSE_RESULTS = "true"
        private const val SGML = "false"
        private const val INIT_FROM_TREES = "false"
        private const val LANG = ""
        private const val TOKENIZER_FACTORY = ""
        private const val XML_INPUT = ""
        private const val TREE_TRANSFORMER = ""
        private const val TREE_NORMALIZER = ""
        private const val TREE_RANGE = ""
        private const val TAG_INSIDE = ""
        private const val APPROXIMATE = "-1.0"
        private const val TOKENIZER_OPTIONS = ""
        private const val DEFAULT_REG_L1 = "1.0"
        private const val OUTPUT_FILE = ""
        private const val OUTPUT_FORMAT = "slashTags"
        private const val OUTPUT_FORMAT_OPTIONS = ""
        private val defaultValues = HashMap<String?, String?>()
        private fun wsvStringToStringArray(str: String?): Array<String?>? {
            return if (str == null || str == "") {
                StringUtils.EMPTY_STRING_ARRAY
            } else {
                str.split("\\s+".toRegex()).toTypedArray()
            }
        }

        /**
         * Prints out the automatically generated props file - in its own
         * method to make code above easier to read
         */
        private fun printGenProps() {
            println("## Sample properties file for maxent tagger. This file is used for three main")
            println("## operations: training, testing, and tagging. It may also be used to convert")
            println("## an old multifile tagger to a single file tagger or to dump the contents of")
            println("## a model.")
            println("## To train or test a model, or to tag something, run:")
            println("##   java edu.stanford.nlp.tagger.maxent.MaxentTagger -prop <properties file>")
            println("## Arguments can be overridden on the commandline, e.g.:")
            println("##   java ....MaxentTagger -prop <properties file> -testFile /other/file ")
            println()
            println("# Model file name (created at train time; used at tag and test time)")
            println("# (you can leave this blank and specify it on the commandline with -model)")
            println("# model = ")
            println()
            println("# Path to file to be operated on (trained from, tested against, or tagged)")
            println("# Specify -textFile <filename> to tag text in the given file, -trainFile <filename> to")
            println("# to train a model using data in the given file, or -testFile <filename> to test your")
            println("# model using data in the given file.  Alternatively, you may specify")
            println("# -dump <filename> to dump the parameters stored in a model or ")
            println("# -convertToSingleFile <filename> to save an old, multi-file model (specified as -model)")
            println("# to the new single file format.  The new model will be saved in the file filename.")
            println("# If you choose to convert an old file, you must specify ")
            println("# the correct 'arch' parameter used to create the original model.")
            println("# trainFile = ")
            println()
            println("# Path to outputFile to write tagged output to.")
            println("# If empty, stdout is used.")
            println("# outputFile = $OUTPUT_FILE")
            println()
            println("# Output format. One of: slashTags (default), xml, or tsv")
            println("# outputFormat = $OUTPUT_FORMAT")
            println()
            println("# Output format options. Comma separated list, but")
            println("# currently \"lemmatize\" is the only supported option.")
            println("# outputFile = $OUTPUT_FORMAT_OPTIONS")
            println()
            println("# Tag separator character that separates word and pos tags")
            println("# (for both training and test data) and used for")
            println("# separating words and tags in slashTags format output.")
            println("# tagSeparator = $TAG_SEPARATOR")
            println()
            println("# Encoding format in which files are stored.  If left blank, UTF-8 is assumed.")
            println("# encoding = $ENCODING")
            println()
            println("# A couple flags for controlling the amount of output:")
            println("# - print extra debugging information:")
            println("# verbose = $VERBOSE")
            println("# - print intermediate results:")
            println("# verboseResults = $VERBOSE_RESULTS")
            println("######### parameters for tag and test operations #########")
            println()
            println("# Class to use for tokenization. Default blank value means Penn Treebank")
            println("# tokenization.  If you'd like to just assume that tokenization has been done,")
            println("# and the input is whitespace-tokenized, use")
            println("# edu.stanford.nlp.process.WhitespaceTokenizer or set tokenize to false.")
            println("# tokenizerFactory = ")
            println()
            println("# Options to the tokenizer.  A comma separated list.")
            println("# This depends on what the tokenizer supports.")
            println("# For PTBTokenizer, you might try options like americanize=false")
            println("# or asciiQuotes (for German!).")
            println("# tokenizerOptions = ")
            println()
            println("# Whether to tokenize text for tag and test operations. Default is true.")
            println("# If false, your text must already be whitespace tokenized.")
            println("# tokenize = $TOKENIZE")
            println()
            println("# Write debugging information (words, top words, unknown words). Useful for")
            println("# error analysis. Default is false.")
            println("# debug = $DEBUG")
            println()
            println("# Prefix for debugging output (if debug == true). Default is to use the")
            println("# filename from 'file'")
            println("# debugPrefix = ")
            println()
            println("######### parameters for training  #########")
            println()
            println("# model architecture: This is one or more comma separated strings, which")
            println("# specify which extractors to use. Some of them take one or more integer")
            println("# or string ")
            println("# (file path) arguments in parentheses, written as m, n, and s below:")
            println("# 'left3words', 'left5words', 'bidirectional', 'bidirectional5words',")
            println("# 'generic', 'sighan2005', 'german', 'words(m,n)', 'wordshapes(m,n)',")
            println("# 'biwords(m,n)', 'lowercasewords(m,n)', 'vbn(n)', distsimconjunction(s,m,n)',")
            println("# 'naacl2003unknowns', 'naacl2003conjunctions', 'distsim(s,m,n)',")
            println("# 'suffix(n)', 'prefix(n)', 'prefixsuffix(n)', 'capitalizationsuffix(n)',")
            println("# 'wordshapes(m,n)', 'unicodeshapes(m,n)', 'unicodeshapeconjunction(m,n)',")
            println("# 'lctagfeatures', 'order(k)', 'chinesedictionaryfeatures(s)'.")
            println("# These keywords determines the features extracted.  'generic' is language independent.")
            println("# distsim: Distributional similarity classes can be an added source of information")
            println("# about your words. An English distsim file is included, or you can use your own.")
            println("# arch = ")
            println()
            println("# 'language'.  This is really the tag set which is used for the")
            println("# list of open-class tags, and perhaps deterministic  tag")
            println("# expansion). Currently we have 'english', 'arabic', 'german', 'chinese'")
            println("# or 'polish' predefined. For your own language, you can specify ")
            println("# the same information via openClassTags or closedClassTags below")
            println("# (only ONE of these three options may be specified). ")
            println("# 'english' means UPenn English treebank tags. 'german' is STTS")
            println("# 'chinese' is CTB, and Arabic is an expanded Bies mapping from the ATB")
            println("# 'polish' means some tags that some guy on the internet once used. ")
            println("# See the TTags class for more information.")
            println("# lang = ")
            println()
            println("# a space-delimited list of open-class parts of speech")
            println("# alternatively, you can specify language above to use a pre-defined list or specify the closed class tags (below)")
            println("# openClassTags = ")
            println()
            println("# a space-delimited list of closed-class parts of speech")
            println("# alternatively, you can specify language above to use a pre-defined list or specify the open class tags (above)")
            println("# closedClassTags = ")
            println()
            println("# A boolean indicating whether you would like the trained model to set POS tags as closed")
            println("# based on their frequency in training; default is false.  The frequency threshold can be set below. ")
            println("# This option is ignored if any of {openClassTags, closedClassTags, lang} are specified.")
            println("# learnClosedClassTags = ")
            println()
            println("# Used only if learnClosedClassTags=true.  Tags that have fewer tokens than this threshold are")
            println("# considered closed in the trained model.")
            println("# closedClassTagThreshold = ")
            println()
            println("# search method for optimization. Normally use the default 'qn'. choices: 'qn' (quasi-Newton),")
            println("# 'cg' (conjugate gradient, 'owlqn' (L1 regularization) or 'iis' (improved iterative scaling)")
            println("# search = $SEARCH")
            println()
            println("# for conjugate gradient or quasi-Newton search, sigma-squared smoothing/regularization")
            println("# parameter. if left blank, the default is 0.5, which is usually okay")
            println("# sigmaSquared = $SIGMA_SQUARED")
            println()
            println("# for OWLQN search, regularization")
            println("# parameter. if left blank, the default is 1.0, which is usually okay")
            println("# regL1 = $DEFAULT_REG_L1")
            println()
            println("# For improved iterative scaling, the number of iterations, otherwise ignored")
            println("# iterations = $ITERATIONS")
            println()
            println("# rare word threshold. words that occur less than this number of")
            println("# times are considered rare words.")
            println("# rareWordThresh = $RARE_WORD_THRESH")
            println()
            println("# minimum feature threshold. features whose history appears less")
            println("# than this number of times are ignored.")
            println("# minFeatureThresh = $MIN_FEATURE_THRESH")
            println()
            println("# current word feature threshold. words that occur more than this")
            println("# number of times will generate features with all of their occurring")
            println("# tags.")
            println("# curWordMinFeatureThresh = $CUR_WORD_MIN_FEATURE_THRESH")
            println()
            println("# rare word minimum feature threshold. features of rare words whose histories")
            println("# appear less than this times will be ignored.")
            println("# rareWordMinFeatureThresh = $RARE_WORD_MIN_FEATURE_THRESH")
            println()
            println("# very common word threshold. words that occur more than this number of")
            println("# times will form an equivalence class by themselves. ignored unless")
            println("# you are using equivalence classes.")
            println("# veryCommonWordThresh = $VERY_COMMON_WORD_THRESH")
            println()
            println("# initFromTrees =")
            println("# treeTransformer =")
            println("# treeNormalizer =")
            println("# treeRange =")
            println("# sgml = ")
            println("# tagInside = ")
        }

        /** Read in a TaggerConfig.
         *
         * @param stream Where to read from
         * @return The TaggerConfig
         * @throws IOException Misc IOError
         * @throws ClassNotFoundException Class error
         */
        fun readConfig(stream: DataInputStream?): TaggerConfig {
            val `in` = ObjectInputStream(stream)
            return `in`.readObject() as TaggerConfig
        }

        /** The directory in a jar file in which to find a tagger resource specified by jar:file  */
        private const val JAR_TAGGER_PATH = "/models/"

        init {
            defaultValues["arch"] = ARCH
            defaultValues["closedClassTags"] = ""
            defaultValues["closedClassTagThreshold"] = CLOSED_CLASS_THRESHOLD
            defaultValues["search"] = SEARCH
            defaultValues["tagSeparator"] = TAG_SEPARATOR
            defaultValues["tokenize"] = TOKENIZE
            defaultValues["debug"] = DEBUG
            defaultValues["iterations"] = ITERATIONS
            defaultValues["rareWordThresh"] = RARE_WORD_THRESH
            defaultValues["minFeatureThresh"] = MIN_FEATURE_THRESH
            defaultValues["curWordMinFeatureThresh"] = CUR_WORD_MIN_FEATURE_THRESH
            defaultValues["rareWordMinFeatureThresh"] = RARE_WORD_MIN_FEATURE_THRESH
            defaultValues["veryCommonWordThresh"] = VERY_COMMON_WORD_THRESH
            defaultValues["occuringTagsOnly"] = OCCURING_TAGS_ONLY
            defaultValues["possibleTagsOnly"] = POSSIBLE_TAGS_ONLY
            defaultValues["sigmaSquared"] = SIGMA_SQUARED
            defaultValues["encoding"] = ENCODING
            defaultValues["learnClosedClassTags"] = LEARN_CLOSED_CLASS
            defaultValues["verbose"] = VERBOSE
            defaultValues["verboseResults"] = VERBOSE_RESULTS
            defaultValues["sgml"] = SGML
            defaultValues["initFromTrees"] = INIT_FROM_TREES
            defaultValues["openClassTags"] = ""
            defaultValues["treeTransformer"] = TREE_TRANSFORMER
            defaultValues["treeNormalizer"] = TREE_NORMALIZER
            defaultValues["lang"] = LANG
            defaultValues["tokenizerFactory"] = TOKENIZER_FACTORY
            defaultValues["xmlInput"] = XML_INPUT
            defaultValues["treeRange"] = TREE_RANGE
            defaultValues["tagInside"] = TAG_INSIDE
            defaultValues["approximate"] = APPROXIMATE
            defaultValues["tokenizerOptions"] = TOKENIZER_OPTIONS
            defaultValues["regL1"] = DEFAULT_REG_L1
            defaultValues["outputFile"] = OUTPUT_FILE
            defaultValues["outputFormat"] = OUTPUT_FORMAT
            defaultValues["outputFormatOptions"] = OUTPUT_FORMAT_OPTIONS
        }

        /** Used for getting tagger models from any of file, URL or jar resource.  */
        fun getTaggerDataInputStream(modelFileOrUrl: String): DataInputStream {
            var `is`: InputStream?
            `is` = if (modelFileOrUrl.matches(Regex("https?://.*"))) {
                val u = URL(modelFileOrUrl)
                DataInputStream(u.openStream())
            } else if (modelFileOrUrl.matches(Regex("jar:.*"))) {
                this::class.java.getResourceAsStream(JAR_TAGGER_PATH + modelFileOrUrl.substring(4)) // length of "jar:"
            } else {
                FileInputStream(modelFileOrUrl)
            }
            if (modelFileOrUrl.endsWith(".gz")) {
                `is` = GZIPInputStream(`is`)
            }
            `is` = BufferedInputStream(`is`)
            return DataInputStream(`is`)
        }
    }

    /**
     * This constructor is just for creating an instance with default values.
     * Used internally.
     */
    private constructor() : super() {
        this.putAll(defaultValues)
    }

    constructor(vararg args: String) : super() {
        val props = Properties()
        props.putAll(argsToProperties(arrayOf(*args)))
        if (props.getProperty("") != null) {
            throw RuntimeException("unknown argument(s): \"" + props.getProperty("") + '\"')
        }
        if (props.getProperty("genprops") != null) {
            printGenProps()
            exitProcess(0)
        }

        // Figure out what mode we're in. First thing to do is check if we're
        // loading a classifier. If so, we need to load the config file from there.
        // We need to check the convertToSingleFile option first, since its
        // presence overrides other matches!
        if (props.containsKey("convertToSingleFile")) {
            mode = Mode.CONVERT
            setProperty("file", props.getProperty("convertToSingleFile").trim { it <= ' ' })
        } else if (props.containsKey("trainFile")) {
            //Training mode
            mode = Mode.TRAIN
            setProperty("file", props.getProperty("trainFile", "").trim { it <= ' ' })
        } else if (props.containsKey("testFile")) {
            //Testing mode
            mode = Mode.TEST
            setProperty("file", props.getProperty("testFile", "").trim { it <= ' ' })
        } else if (props.containsKey("textFile")) {
            //Tagging mode
            mode = Mode.TAG
            setProperty("file", props.getProperty("textFile", "").trim { it <= ' ' })
        } else if (props.containsKey("dump")) {
            mode = Mode.DUMP
            setProperty("file", props.getProperty("dump").trim { it <= ' ' })
            props.setProperty("model", props.getProperty("dump").trim { it <= ' ' })
        } else {
            mode = Mode.TAG
            setProperty("file", "stdin")
        }
        //for any mode other than train, we load a classifier, which means we load a config - model always needs to be specified
        //on command line/in props file
        //Get the path to the model (or the path where you'd like to save the model); this is necessary for training, testing, and tagging
        setProperty("model", props.getProperty("model", "").trim { it <= ' ' })
        if (mode != Mode.DUMP && this.getProperty("model") == "") {
            throw RuntimeException("'model' parameter must be specified")
        }

        /* Try and use the default properties from the model */
        //Properties modelProps = new Properties();
        val oldConfig = TaggerConfig() // loads default values in oldConfig
        if (mode != Mode.TRAIN && mode != Mode.CONVERT) {
            try {
                val `in` = getTaggerDataInputStream(getProperty("model"))
                oldConfig.putAll(readConfig(`in`)) // overwrites defaults with any serialized values.
                `in`.close()
            } catch (e: Exception) {
                System.err.println("Error: No such trained tagger config file found.")
                e.printStackTrace()
            }
        }
        setProperty("search", props.getProperty("search", oldConfig.getProperty("search")).trim { it <= ' ' }.toLowerCase())
        val srch = this.getProperty("search")
        if (!(srch == "cg" || srch == "iis" || srch == "owlqn" || srch == "qn")) {
            throw RuntimeException("'search' must be one of 'iis', 'cg', 'qn' or 'owlqn': $srch")
        }
        setProperty("sigmaSquared", props.getProperty("sigmaSquared", oldConfig.getProperty("sigmaSquared")))
        setProperty("tagSeparator", props.getProperty("tagSeparator", oldConfig.getProperty("tagSeparator")))
        setProperty("iterations", props.getProperty("iterations", oldConfig.getProperty("iterations")))
        setProperty("rareWordThresh", props.getProperty("rareWordThresh", oldConfig.getProperty("rareWordThresh")))
        setProperty("minFeatureThresh", props.getProperty("minFeatureThresh", oldConfig.getProperty("minFeatureThresh")))
        setProperty("curWordMinFeatureThresh", props.getProperty("curWordMinFeatureThresh", oldConfig.getProperty("curWordMinFeatureThresh")))
        setProperty("rareWordMinFeatureThresh", props.getProperty("rareWordMinFeatureThresh", oldConfig.getProperty("rareWordMinFeatureThresh")))
        setProperty("veryCommonWordThresh", props.getProperty("veryCommonWordThresh", oldConfig.getProperty("veryCommonWordThresh")))
        setProperty("occuringTagsOnly", props.getProperty("occuringTagsOnly", oldConfig.getProperty("occuringTagsOnly")))
        setProperty("possibleTagsOnly", props.getProperty("possibleTagsOnly", oldConfig.getProperty("possibleTagsOnly")))
        setProperty("lang", props.getProperty("lang", oldConfig.getProperty("lang")))
        setProperty("openClassTags", props.getProperty("openClassTags", oldConfig.getProperty("openClassTags")).trim { it <= ' ' })
        setProperty("closedClassTags", props.getProperty("closedClassTags", oldConfig.getProperty("closedClassTags")).trim { it <= ' ' })
        setProperty("learnClosedClassTags", props.getProperty("learnClosedClassTags", oldConfig.getProperty("learnClosedClassTags")))
        setProperty("closedClassTagThreshold", props.getProperty("closedClassTagThreshold", oldConfig.getProperty("closedClassTagThreshold")))
        setProperty("arch", props.getProperty("arch", oldConfig.getProperty("arch")))
        setProperty("tokenize", props.getProperty("tokenize", oldConfig.getProperty("tokenize")))
        setProperty("tokenizerFactory", props.getProperty("tokenizerFactory", oldConfig.getProperty("tokenizerFactory")))
        setProperty("debugPrefix", props.getProperty("debugPrefix", oldConfig.getProperty("debugPrefix", "")))
        setProperty("debug", props.getProperty("debug", DEBUG))
        setProperty("encoding", props.getProperty("encoding", oldConfig.getProperty("encoding")))
        setProperty("sgml", props.getProperty("sgml", oldConfig.getProperty("sgml")))
        setProperty("verbose", props.getProperty("verbose", oldConfig.getProperty("verbose")))
        setProperty("verboseResults", props.getProperty("verboseResults", oldConfig.getProperty("verboseResults")))
        setProperty("initFromTrees", props.getProperty("initFromTrees", oldConfig.getProperty("initFromTrees")))
        setProperty("treeRange", props.getProperty("treeRange", oldConfig.getProperty("treeRange")))
        setProperty("treeTransformer", props.getProperty("treeTransformer", oldConfig.getProperty("treeTransformer")))
        setProperty("treeNormalizer", props.getProperty("treeNormalizer", oldConfig.getProperty("treeNormalizer")))
        setProperty("regL1", props.getProperty("regL1", oldConfig.getProperty("regL1")))

        //this is a property that is stored (not like the general properties)
        setProperty("xmlInput", props.getProperty("xmlInput", oldConfig.getProperty("xmlInput")).trim { it <= ' ' })
        setProperty("tagInside", props.getProperty("tagInside", oldConfig.getProperty("tagInside"))) //this isn't something we save from time to time
        setProperty("approximate", props.getProperty("approximate", oldConfig.getProperty("approximate"))) //this isn't something we save from time to time
        setProperty("tokenizerOptions", props.getProperty("tokenizerOptions", oldConfig.getProperty("tokenizerOptions"))) //this isn't something we save from time to time
        setProperty("outputFile", props.getProperty("outputFile", oldConfig.getProperty("outputFile")).trim { it <= ' ' }) //this isn't something we save from time to time
        setProperty("outputFormat", props.getProperty("outputFormat", oldConfig.getProperty("outputFormat")).trim { it <= ' ' }) //this isn't something we save from time to time
        setProperty("outputFormatOptions", props.getProperty("outputFormatOptions", oldConfig.getProperty("outputFormatOptions")).trim { it <= ' ' }) //this isn't something we save from time to time
    }

    val rareWordThresh: Int
        get() = getProperty("rareWordThresh").toInt()

    val lang: String
        get() = getProperty("lang")

    val openClassTags: Array<String?>?
        get() = wsvStringToStringArray(getProperty("openClassTags"))

    val closedClassTags: Array<String?>?
        get() = wsvStringToStringArray(getProperty("closedClassTags"))

    val learnClosedClassTags: Boolean
        get() = java.lang.Boolean.parseBoolean(getProperty("learnClosedClassTags"))

    /**
     * Returns a default score to be used for each tag that is incompatible with
     * the current word (e.g., the tag CC for the word "apple"). Using a default
     * score may slightly decrease performance for some languages (e.g., Chinese and
     * German), but allows the tagger to run considerably faster (since the computation
     * of the normalization term Z requires much less feature extraction). This approximation
     * does not decrease performance in English (on the WSJ). If this function returns
     * 0.0, the tagger will compute exact scores.
     *
     * @return default score
     */
    val defaultScore: Double
        get() {
            val approx = getProperty("approximate")
            return if ("false".equals(approx, ignoreCase = true)) {
                -1.0
            } else if ("true".equals(approx, ignoreCase = true)) {
                1.0
            } else {
                approx.toDouble()
            }
        }

    private fun dump(pw: PrintWriter) {
        pw.println("                   model = " + getProperty("model"))
        pw.println("                    arch = " + getProperty("arch"))
        if (mode == Mode.TRAIN) {
            pw.println("               trainFile = " + getProperty("file"))
        } else if (mode == Mode.TAG) {
            pw.println("                textFile = " + getProperty("file"))
        } else if (mode == Mode.TEST) {
            pw.println("                testFile = " + getProperty("file"))
        }
        pw.println("         closedClassTags = " + getProperty("closedClassTags"))
        pw.println(" closedClassTagThreshold = " + getProperty("closedClassTagThreshold"))
        pw.println(" curWordMinFeatureThresh = " + getProperty("curWordMinFeatureThresh"))
        pw.println("                   debug = " + getProperty("debug"))
        pw.println("             debugPrefix = " + getProperty("debugPrefix"))
        pw.println("            tagSeparator = " + getProperty("tagSeparator"))
        pw.println("                encoding = " + getProperty("encoding"))
        pw.println("           initFromTrees = " + getProperty("initFromTrees"))
        pw.println("              iterations = " + getProperty("iterations"))
        pw.println("                    lang = " + getProperty("lang"))
        pw.println("    learnClosedClassTags = " + getProperty("learnClosedClassTags"))
        pw.println("        minFeatureThresh = " + getProperty("minFeatureThresh"))
        pw.println("           openClassTags = " + getProperty("openClassTags"))
        pw.println("rareWordMinFeatureThresh = " + getProperty("rareWordMinFeatureThresh"))
        pw.println("          rareWordThresh = " + getProperty("rareWordThresh"))
        pw.println("                  search = " + getProperty("search"))
        pw.println("                    sgml = " + getProperty("sgml"))
        pw.println("            sigmaSquared = " + getProperty("sigmaSquared"))
        pw.println("                   regL1 = " + getProperty("regL1"))
        pw.println("               tagInside = " + getProperty("tagInside"))
        pw.println("                tokenize = " + getProperty("tokenize"))
        pw.println("        tokenizerFactory = " + getProperty("tokenizerFactory"))
        pw.println("        tokenizerOptions = " + getProperty("tokenizerOptions"))
        pw.println("               treeRange = " + getProperty("treeRange"))
        pw.println("          treeNormalizer = " + getProperty("treeNormalizer"))
        pw.println("         treeTransformer = " + getProperty("treeTransformer"))
        pw.println("                 verbose = " + getProperty("verbose"))
        pw.println("          verboseResults = " + getProperty("verboseResults"))
        pw.println("    veryCommonWordThresh = " + getProperty("veryCommonWordThresh"))
        pw.println("                xmlInput = " + getProperty("xmlInput"))
        pw.println("              outputFile = " + getProperty("outputFile"))
        pw.println("            outputFormat = " + getProperty("outputFormat"))
        pw.println("     outputFormatOptions = " + getProperty("outputFormatOptions"))
        pw.flush()
    }

    override fun toString(): String {
        val sw = StringWriter(200)
        val pw = PrintWriter(sw)
        dump(pw)
        return sw.toString()
    }

}