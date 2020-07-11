import com.github.ojj11.MaxentTagger
import com.github.ojj11.PureParameters

@JsExport
@JsName("readModelSync")
fun readModelSync(file: String): dynamic {
    return js("""
        require("zlib").gunzipSync(require("fs").readFileSync(__dirname + "/../../../../../models/" + file + ".cbor.gz"))
    """)
}

@JsExport
data class Output(val word: String, var tag: String?)

@JsExport
@JsName("tagger")
class Tagger(bytes: ByteArray) {
    private val dataSerializer = PureParameters.serializer()
    private val cbor = kotlinx.serialization.cbor.Cbor()
    private val tagger = MaxentTagger(cbor.load(dataSerializer, bytes))

    @JsName("tag")
    fun tag(words: Array<String>) = tagger.tagSentence(words).map {
        Output(it.word, it.tag)
    }.toTypedArray()
}