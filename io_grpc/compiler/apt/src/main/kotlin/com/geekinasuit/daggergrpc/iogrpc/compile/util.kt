package com.geekinasuit.daggergrpc.iogrpc.compile

import com.squareup.javapoet.JavaFile

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
