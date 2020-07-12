package com.github.ojj11

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream

/** Application to convert model formats to the Gzipped CBOR format preferred */
internal object ConvertModels {
    @JvmStatic
    fun main(args: Array<String>) {

        val dataSerializer = PureParameters.serializer()
        val cbor = kotlinx.serialization.cbor.Cbor()

        (File("originalModels/").listFiles() ?: return).toList().filter {
            it.absolutePath.endsWith(".tagger")
        }.forEach { input ->
            println("Converting ${input.absolutePath}")
            val model = JavaLoadingTools.load(input.absolutePath)
            val outputName = "models/${input.nameWithoutExtension}.cbor.gz"
            val writer = GZIPOutputStream(BufferedOutputStream(FileOutputStream(outputName)))
            writer.write(cbor.dump(dataSerializer, model))
            writer.close()
        }
    }
}