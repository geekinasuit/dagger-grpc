package com.geekinasuit.daggergrpc.iogrpc.compile

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference

/**
 * Metadata needed to generate the GRPC application graph elements, including the handler modules,
 * the callscope graph interface (subcomponent), etc.
 */
data class ApplicationMetadata(
  val packageName: String,
  val handlers: List<HandlerMetadata>,
  var clazz: KSClassDeclaration,
  val callScopedModules: List<KSTypeReference>,
)

/** Metadata needed to generate a handler adapter. */
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
