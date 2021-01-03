import com.github.ojj11.MaxentTagger
import com.github.ojj11.PureParameters
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.IllegalStateException

@ExperimentalJsExport
@JsExport
/** Exportable copy of [TaggedWord] */
data class Output(val word: String, var tag: String)

@ExperimentalJsExport
@ExperimentalSerializationApi
@JsExport
@JsName("tagger")
class Tagger(bytes: ByteArray) {
    private val dataSerializer = PureParameters.serializer()
    private val cbor = kotlinx.serialization.cbor.Cbor.Default
    private val tagger = MaxentTagger(cbor.decodeFromByteArray(dataSerializer, bytes))

    @Suppress("unused")
    @JsName("tag")
    fun tag(words: Array<String>) = tagger.tagSentence(words).map {
        Output(
            word = it.word,
            tag = it.tag ?: throw IllegalStateException("Expected non-null tag for '${it.word}'")
        )
    }.toTypedArray()
}
