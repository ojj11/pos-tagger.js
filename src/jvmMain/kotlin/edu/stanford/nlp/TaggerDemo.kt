package edu.stanford.nlp

import edu.stanford.nlp.JavaLoadingTools.taggerConfig
import edu.stanford.nlp.io.PureParameters
import edu.stanford.nlp.ling.Sentence
import edu.stanford.nlp.ling.Word
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import edu.stanford.nlp.tagger.maxent.TaggerConfig
import java.io.*
import java.util.zip.GZIPOutputStream

internal object TaggerDemo {
    @JvmStatic
    fun main(args: Array<String>) {

        val parameters = PureParameters(
                JavaLoadingTools.fromFile(
                        "/Users/oljones/Downloads/stanford-postagger-2010-05-26 3/models/left3words-wsj-0-18.tagger"
                ),
                TaggerConfig("-model", "/Users/oljones/Downloads/stanford-postagger-2010-05-26 3/models/left3words-wsj-0-18.tagger").taggerConfig()
        )

        val dataSerializer = PureParameters.serializer()
        val cbor = kotlinx.serialization.cbor.Cbor()
        val writer = BufferedOutputStream(FileOutputStream("model.cbor"))
        val dump: ByteArray = cbor.dump(dataSerializer, parameters)
        writer.write(dump)
        writer.close()

        val tagger = MaxentTagger(cbor.load(dataSerializer, dump))

        val sentences = listOf(
                listOf(
                        Word("this"),
                        Word("is"),
                        Word("a"),
                        Word("lot"),
                        Word("slower"),
                        Word("than"),
                        Word("I"),
                        Word("remember")
                        )
        )
        for (sentence in sentences) {
            val tSentence = tagger.tagSentence(sentence)
            println(Sentence.listToString(tSentence, false))
        }
    }
}