package com.geekinasuit.daggergrpc.iogrpc.compile

import com.squareup.kotlinpoet.FileSpec

fun FileSpec.writeToString(): String {
  val str =
    StringBuilder()
      .apply {
        this.append("${relativePath}\n")
        this@writeToString.writeTo(this@apply)
      }
      .toString()
      .replace("\n", "\n    ")
  return str
}
