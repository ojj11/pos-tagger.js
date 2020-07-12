import com.github.ojj11.MaxentTagger
import com.github.ojj11.PureParameters

/** Exportable copy of [TaggedWord] */
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