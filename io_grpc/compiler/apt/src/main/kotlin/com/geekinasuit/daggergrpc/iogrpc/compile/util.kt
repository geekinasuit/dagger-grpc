package com.geekinasuit.daggergrpc.iogrpc.compile

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.JavaFile
import javax.annotation.processing.Generated

fun JavaFile.writeToString(): String {
  val str =
    StringBuilder()
      .apply {
        this.append("${this@writeToString.typeSpec.name}\n")
        this@writeToString.writeTo(this@apply)
      }
      .toString()
      .replace("\n", "\n    ")
  return str
}

fun generatedAnnotation(): AnnotationSpec =
  AnnotationSpec.builder(Generated::class.java)
    .addMember("value", "\"${DaggerGrpcAPTProcessor::class.qualifiedName}\"")
    .build()

fun String.lowerFirst() = this[0].lowercase() + this.substring(1)
