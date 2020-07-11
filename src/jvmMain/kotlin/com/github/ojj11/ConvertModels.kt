package com.github.ojj11

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream

internal object ConvertModels {
    @JvmStatic
    fun main(args: Array<String>) {

        val dataSerializer = PureParameters.serializer()
        val cbor = kotlinx.serialization.cbor.Cbor()

        File("originalModels/").listFiles().toList().filter {
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