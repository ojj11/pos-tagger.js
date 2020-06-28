import edu.stanford.nlp.io.PureParameters
import edu.stanford.nlp.ling.Word
import edu.stanford.nlp.tagger.maxent.MaxentTagger

data class OutputWord(
        val word: String,
        val tag: String?
)

@JsName("bidirectional_distsim_wsj_v0_18")
fun getBundledBidirectional() = js("""
    require("zlib").gunzipSync(require("fs").readFileSync(__dirname + "/../../../../../models/bidirectional-distsim-wsj-0-18.cbor.gz"))
""")

@JsName("left3words_wsj_v0_18")
fun getBundledUnidirectional() = js("""
    require("zlib").gunzipSync(require("fs").readFileSync(__dirname + "/../../../../../models/left3words-wsj-0-18.cbor.gz"))
""")

@JsName("tagger")
class Tagger(model: ByteArray) {
    val dataSerializer = PureParameters.serializer()
    val cbor = kotlinx.serialization.cbor.Cbor()
    val tagger = MaxentTagger(cbor.load(dataSerializer, model))

    @Suppress("unused")
    @JsName("tag")
    fun tag(words: Array<String>): Array<OutputWord> {
        return tagger.tagSentence(words.map {Word(it)})
                .map { OutputWord(it.word(), it.tag()) }
                .toTypedArray()
    }
}