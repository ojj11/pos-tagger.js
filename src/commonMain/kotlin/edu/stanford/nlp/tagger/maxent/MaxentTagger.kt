package edu.stanford.nlp.tagger.maxent

import edu.stanford.nlp.io.PureModel
import edu.stanford.nlp.io.PureParameters
import edu.stanford.nlp.io.PureTaggerConfig
import edu.stanford.nlp.ling.HasWord
import edu.stanford.nlp.ling.TaggedWord
import kotlin.math.max

/**
 * The main class for users to run, train, and test the part of speech tagger.
 *
 * You can tag things through the Java API or from the command line.
 * The two English taggers included in this distribution are:
 *
 *  *  A bi-directional dependency network tagger in models/bidirectional-distsim-wsj-0-18.tagger.
 * Its accuracy was 97.32% on Penn Treebank WSJ secs. 22-24.
 *  *  A model using only left sequence information and similar but less
 * unknown words and lexical features as the previous model in
 * models/left3words-wsj-0-18.tagger. This tagger runs a lot faster.
 * Its accuracy was 96.92% on Penn Treebank WSJ secs. 22-24.
 *
 *
 * <h3>Using the Java API</h3>
 * <dl>
 * <dt>
 * A MaxentTagger can be made with a constructor taking as argument the location of parameter files for a trained tagger: </dt>
 * <dd> `MaxentTagger tagger = new MaxentTagger("models/left3words-wsj-0-18.tagger");`</dd>
 *
 *
 * <dt>A default path is provided for the location of the tagger on the Stanford NLP machines:</dt>
 * <dd>`MaxentTagger tagger = new MaxentTagger(DEFAULT_NLP_GROUP_MODEL_PATH); `</dd>
 *
 *
 * <dt>If you set the NLP_TAGGER_HOME environment variable,
 * DEFAULT_NLP_GROUP_MODEL_PATH will instead point to the directory
 * given in NLP_TAGGER_HOME.</dt>
 *
 *
 * <dt>To tag a Sentence and get a TaggedSentence: </dt>
 * <dd>`Sentence taggedSentence = tagger.tagSentence(Sentence sentence)`</dd>
 * <dd>`Sentence taggedSentence = tagger.apply(Sentence sentence)`</dd>
 *
 *
 * <dt>To tag a list of sentences and get back a list of tagged sentences:
</dt> * <dd>` List taggedList = tagger.process(List sentences)`</dd>
 *
 *
 * <dt>To tag a String of text and to get back a String with tagged words:</dt>
 * <dd> `String taggedString = tagger.tagString("Here's a tagged string.")`</dd>
 *
 *
 * <dt>To tag a string of *correctly tokenized*, whitespace-separated words and get a string of tagged words back:</dt>
 * <dd> `String taggedString = tagger.tagTokenizedString("Here 's a tagged string .")`</dd>
</dl> *
 *
 *
 * The `tagString` method uses the default tokenizer (PTBTokenizer).
 * If you wish to control tokenization, you may wish to call
 * `process()` on the result.
 *
 *
 * <h3>Using the command line</h3>
 *
 * Tagging, testing, and training can all also be done via the command line.
 * <h3>Training from the command line</h3>
 * To train a model from the command line, first generate a property file:
 * <pre>java edu.stanford.nlp.tagger.maxent.MaxentTagger -genprops </pre>
 *
 * This gets you a default properties file with descriptions of each parameter you can set in
 * your trained model.  You can modify the properties file , or use the default options.  To train, run:
 * <pre>java -mx1g edu.stanford.nlp.tagger.maxent.MaxentTagger -props myPropertiesFile.props </pre>
 *
 * with the appropriate properties file specified; any argument you give in the properties file can also
 * be specified on the command line.  You must have specified a model using -model, either in the properties file
 * or on the command line, as well as a file containing tagged words using -trainFile.
 *
 * Useful flags for controlling the amount of output are -verbose, which prints extra debugging information,
 * and -verboseResults, which prints full information about intermediate results.  -verbose defaults to false
 * and -verboseResults defaults to true.
 *
 * <h3>Tagging and Testing from the command line</h3>
 *
 * Usage:
 * For tagging (plain text):
 * <pre>java edu.stanford.nlp.tagger.maxent.MaxentTagger -model &lt;modelFile&gt; -textFile &lt;textfile&gt; </pre>
 * For testing (evaluating against tagged text):
 * <pre>java edu.stanford.nlp.tagger.maxent.MaxentTagger -model &lt;modelFile&gt; -testFile &lt;testfile&gt; </pre>
 * You can use the same properties file as for training
 * if you pass it in with the "-props" argument. The most important
 * arguments for tagging (besides "model" and "file") are "tokenize"
 * and "tokenizerFactory". See below for more details.
 *
 * Note that the tagger assumes input has not yet been tokenized and by default tokenizes it using a default
 * English tokenizer.  If your input has already been tokenized, use the flag "-tokenized".
 *
 *
 *  Parameters can be defined using a Properties file
 * (specified on the command-line with `-prop` *propFile*),
 * or directly on the command line (by preceding their name with a minus sign
 * ("-") to turn them into a flag. The following properties are recognized:
 *
 * <table border="1">
 * <tr><td>**Property Name**</td><td>**Type**</td><td>**Default Value**</td><td>**Relevant Phase(s)**</td><td>**Description**</td></tr>
 * <tr><td>model</td><td>String</td><td>N/A</td><td>All</td><td>Path and filename where you would like to save the model (training) or where the model should be loaded from (testing, tagging).</td></tr>
 * <tr><td>trainFile</td><td>String</td><td>N/A</td><td>Train</td><td>Path to the file holding the training data; specifying this option puts the tagger in training mode.  Only one of 'trainFile','testFile','texFile', and 'convertToSingleFile' may be specified.</td></tr>
 * <tr><td>testFile</td><td>String</td><td>N/A</td><td>Test</td><td>Path to the file holding the test data; specifying this option puts the tagger in testing mode.  Only one of 'trainFile','testFile','texFile', and 'convertToSingleFile' may be specified.</td></tr>
 * <tr><td>textFile</td><td>String</td><td>N/A</td><td>Tag</td><td>Path to the file holding the text to tag; specifying this option puts the tagger in tagging mode.  Only one of 'trainFile','testFile','textFile', and 'convertToSingleFile' may be specified.</td></tr>
 * <tr><td>convertToSingleFile</td><td>String</td><td>N/A</td><td>N/A</td><td>Provided only for backwards compatibility, this option allows you to convert a tagger trained using a previous version of the tagger to the new single-file format.  The value of this flag should be the path for the new model file, 'model' should be the path prefix to the old tagger (up to but not including the ".holder"), and you should supply the properties configuration for the old tagger with -props (before these two arguments).</td></tr>
 * <tr><td>genprops</td><td>boolean</td><td>N/A</td><td>N/A</td><td>Use this option to output a default properties file, containing information about each of the possible configuration options.</td></tr>
 * <tr><td>delimiter</td><td>char</td><td>/</td><td>All</td><td>Delimiter character that separates word and part of speech tags.  For training and testing, this is the delimiter used in the train/test files.  For tagging, this is the character that will be inserted between words and tags in the output.</td></tr>
 * <tr><td>encoding</td><td>String</td><td>UTF-8</td><td>All</td><td>Encoding of the read files (training, testing) and the output text files.</td></tr>
 * <tr><td>tokenize</td><td>boolean</td><td>true</td><td>Tag,Test</td><td>Whether or not the file has been tokenized.  If this is true, the tagger assumes that white space separates all and only those things that should be tagged as separate tokens, and that the input is strictly one sentence per line.</td></tr>
 * <tr><td>tokenizerFactory</td><td>String</td><td>edu.stanford.nlp.process.PTBTokenizer</td><td>Tag,Test</td><td>Fully qualified class name of the tokenizer to use.  edu.stanford.nlp.process.PTBTokenizer does basic English tokenization.</td></tr>
 * <tr><td>tokenizerOptions</td><td>String</td><td></td><td>Tag,Test</td><td>Known options for the particular tokenizer used. A comma-separated list. For PTBTokenizer, options of interest include `americanize=false` and `asciiQuotes` (for German). Note that any choice of tokenizer options that conflicts with the tokenization used in the tagger training data will likely degrade tagger performance.</td></tr>
 * <tr><td>arch</td><td>String</td><td>generic</td><td>Train</td><td>Architecture of the model, as a comma-separated list of options, some with a parenthesized integer argument written k here: this determines what features are sed to build your model.  Options are 'left3words', 'left5words', 'bidirectional', 'bidirectional5words', generic', 'sighan2005' (Chinese), 'german', 'words(k),' 'naacl2003unknowns', 'naacl2003conjunctions', wordshapes(k), motleyUnknown, suffix(k), prefix(k), prefixsuffix(k), capitalizationsuffix(k), distsim(s), chinesedictionaryfeatures(s), lctagfeatures, unicodeshapes(k). The left3words architectures are faster, but slightly less accurate, than the bidirectional architectures.  'naacl2003unknowns' was our traditional set of unknown word features, but you can now specify features more flexibility via the various other supported keywords. The 'shapes' options map words to equivalence classes, which slightly increase accuracy.</td></tr>
 * <tr><td>lang</td><td>String</td><td>english</td><td>Train</td><td>Language from which the part of speech tags are drawn. This option determines which tags are considered closed-class (only fixed set of words can be tagged with a closed-class tag, such as prepositions). Defined languages are 'english' (Penn tagset), 'polish' (very rudimentary), 'chinese', 'arabic', 'german', and 'medline'.  </td></tr>
 * <tr><td>openClassTags</td><td>String</td><td>N/A</td><td>Train</td><td>Space separated list of tags that should be considered open-class.  All tags encountered that are not in this list are considered closed-class.  E.g. format: "NN VB"</td></tr>
 * <tr><td>closedClassTags</td><td>String</td><td>N/A</td><td>Train</td><td>Space separated list of tags that should be considered closed-class.  All tags encountered that are not in this list are considered open-class.</td></tr>
 * <tr><td>learnClosedClassTags</td><td>boolean</td><td>false</td><td>Train</td><td>If true, induce which tags are closed-class by counting as closed-class tags all those tags which have fewer unique word tokens than closedClassTagThreshold. </td></tr>
 * <tr><td>closedClassTagThreshold</td><td>int</td><td>int</td><td>Train</td><td>Number of unique word tokens that a tag may have and still be considered closed-class; relevant only if learnClosedClassTags is true.</td></tr>
 * <tr><td>sgml</td><td>boolean</td><td>false</td><td>Tag, Test</td><td>Very basic tagging of the contents of all sgml fields; for more complex mark-up, consider using the xmlInput option.</td></tr>
 * <tr><td>xmlInput</td><td>String</td><td></td><td>Tag, Test</td><td>Give a space separated list of tags in an XML file whose content you would like tagged.  Any internal tags that appear in the content of fields you would like tagged will be discarded; the rest of the XML will be preserved and the original text of specified fields will be replaced with the tagged text.</td></tr>
 * <tr><td>xmlOutput</td><td>String</td><td>""</td><td>Tag</td><td>If a path is given, the tagged data be written out to the given file in xml.  If non-empty, each word will be written out within a word tag, with the part of speech as an attribute.  If original input was XML, this will just appear in the field where the text originally came from.  Otherwise, word tags will be surrounded by sentence tags as well.  E.g., &lt;sentence id="0"&gt;&lt;word id="0" pos="NN"&gt;computer&lt;/word&gt;&lt;/sentence&gt;</td></tr>
 * <tr><td>tagInside</td><td>String</td><td>""</td><td>Tag</td><td>Tags inside elements that match the regular expression given in the String.</td></tr>
 * <tr><td>search</td><td>String</td><td>cg</td><td>Train</td><td>Specify the search method to be used in the optimization method for training.  Options are 'cg' (conjugate gradient) or 'iis' (improved iterative scaling).</td></tr>
 * <tr><td>sigmaSquared</td><td>double</td><td>0.5</td><td>Train</td><td>Sigma-squared smoothing/regularization parameter to be used for conjugate gradient search.  Default usually works reasonably well.</td></tr>
 * <tr><td>iterations</td><td>int</td><td>100</td><td>Train</td><td>Number of iterations to be used for improved iterative scaling.</td></tr>
 * <tr><td>rareWordThresh</td><td>int</td><td>5</td><td>Train</td><td>Words that appear fewer than this number of times during training are considered rare words and use extra rare word features.</td></tr>
 * <tr><td>minFeatureThreshold</td><td>int</td><td>5</td><td>Train</td><td>Features whose history appears fewer than this number of times are discarded.</td></tr>
 * <tr><td>curWordMinFeatureThreshold</td><td>int</td><td>2</td><td>Train</td><td>Words that occur more than this number of times will generate features with all of the tags they've been seen with.</td></tr>
 * <tr><td>rareWordMinFeatureThresh</td><td>int</td><td>10</td><td>Train</td><td>Features of rare words whose histories occur fewer than this number of times are discarded.</td></tr>
 * <tr><td>veryCommonWordThresh</td><td>int</td><td>250</td><td>Train</td><td>Words that occur more than this number of times form an equivalence class by themselves.  Ignored unless you are using ambiguity classes.</td></tr>
 * <tr><td>debug</td><td>boolean</td><td>boolean</td><td>All</td><td>Whether to write debugging information (words, top words, unknown words).  Useful for error analysis.</td></tr>
 * <tr><td>debugPrefix</td><td>String</td><td>N/A</td><td>All</td><td>File (path) prefix for where to write out the debugging information (relevant only if debug=true).</td></tr>
</table> *
 *
 *
 *
 * @author Kristina Toutanova
 * @author Miler Lee
 * @author Joseph Smarr
 * @author Anna Rafferty
 * @author Michel Galley
 * @author Christopher Manning
 * @author John Bauer
 */
class MaxentTagger(
        parameters: PureParameters
) {

    val dict: Dictionary
    val tags: TTags
    val extractors: PureExtractors
    val extractorsRare: PureExtractors
    val lambdaSolve: LambdaSolveTagger

    var defaultScore = 0.0
    var leftContext = 0
    var rightContext = 0
    var ySize = 0

    private val fAssociations: Map<FeatureKey, Int>
    private val rareWordThresh: Int

    fun getNum(s: FeatureKey) = fAssociations[s] ?: -1

    private fun setExtractorsGlobal() {
        extractors.setGlobalHolder(this)
        extractorsRare.setGlobalHolder(this)
    }

    fun isRare(word: String?) = dict.sum(word) < rareWordThresh

    /**
     * Returns a new Sentence that is a copy of the given sentence with all the
     * words tagged with their part-of-speech. Convenience method when you only
     * want to tag a single Sentence instead of a Document of sentences.
     * @param sentence sentence to tag
     * @return tagged sentence
     */
    fun tagSentence(sentence: List<HasWord>): List<TaggedWord> {
        val testSentence = TestSentence(this)
        return testSentence.tagSentence(sentence)
    }

    companion object {
        const val RARE_WORD_THRESH = 5
        const val MIN_FEATURE_THRESH = 5
        const val CUR_WORD_MIN_FEATURE_THRESH = 2
        const val RARE_WORD_MIN_FEATURE_THRESH = 10
        const val VERY_COMMON_WORD_THRESH = 250
        const val OCCURRING_TAGS_ONLY = false
        const val POSSIBLE_TAGS_ONLY = false
    }

    init {
        val config = parameters.config
        val lang: String = config.lang
        val openClassTags = config.openClassTags
        val closedClassTags = config.closedClassTags
        rareWordThresh = config.rareWordThresh
        defaultScore = if (lang == "english") 1.0 else 0.0
        if (config.defaultScore >= 0) defaultScore = config.defaultScore

        if (openClassTags!!.isNotEmpty() && lang != "" || closedClassTags!!.isNotEmpty() && lang != "" || closedClassTags.isNotEmpty() && openClassTags.isNotEmpty()) {
            throw RuntimeException("At least two of lang (\"" + lang + "\"), openClassTags (length " + openClassTags.size + ": " + openClassTags.contentToString() + ")," +
                    "and closedClassTags (length " + closedClassTags!!.size + ": " + closedClassTags.contentToString() + ") specified---you must choose one!")
        } else if (openClassTags.isEmpty() && lang == "" && closedClassTags.isEmpty() && !config.learnClosedClassTags) {

        }

        tags = when {
            openClassTags.isNotEmpty() -> {
                TTags(openClassTags = openClassTags.filterNotNull().toMutableSet())
            }
            closedClassTags.isNotEmpty() -> {
                TTags(closedClassTags = closedClassTags.filterNotNull().toMutableSet())
            }
            else -> {
                TTags(lang)
            }
        }

        val model = parameters.model
        ySize = model.ySize
        extractors = model.extractors
        extractorsRare = model.extractorsRare
        fAssociations = model.fAssociations
        lambdaSolve = model.lambdaSolve
        dict = model.dict
        tags.merge(model.tags, model.closedClassTags)

        extractors.initTypes()
        extractorsRare.initTypes()

        leftContext = max(extractors.leftContext(), extractorsRare.leftContext())
        rightContext = max(extractors.rightContext(), extractorsRare.rightContext())
        setExtractorsGlobal()
    }
}