package com.geekinasuit.daggergrpc.iogrpc.compile

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration

data class HandlerMetadata(
  val ast: KSClassDeclaration,
  val annotation: KSAnnotation,
  val name: String,
  val packageName: String,
  val grpcClass: KSClassDeclaration,
  val serviceInterface: KSClassDeclaration,
) {
  val qualifiedName: String
    get() = "$packageName.$name"

  val adapterName: String
    get() = "${name}Adapter"
}
