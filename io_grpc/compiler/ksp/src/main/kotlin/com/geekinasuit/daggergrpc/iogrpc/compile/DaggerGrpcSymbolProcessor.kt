package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.daggergrpc.api.GrpcServiceHandler
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class DaggerGrpcSymbolProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val validator = Validator(env.logger)
    val symbols = resolver.getSymbolsWithAnnotation(GrpcServiceHandler::class.qualifiedName!!)
    val unprocessable = mutableListOf<KSAnnotated>()
    val handlerMetadatas =
      symbols
        .onEach { env.logger.info("Processing $it") }
        .onEach { if (it !is KSClassDeclaration) unprocessable += it }
        .filterIsInstance<KSClassDeclaration>()
        .map(validator::validate)
        .filterNotNull()
        .toList()
    handlerMetadatas.forEach(env::generateAdapter)
    if (handlerMetadatas.isEmpty()) {
      env.logger.warn("No valid classes were annotated with @GrpcServiceHandler")
    } else {
      val targetPackage = env.options["daggergrpc.package"]
      if (targetPackage == null) {
        env.logger.warn("daggergrpc.package option not set; skipping module generation")
      } else {
        env.generateModule(handlerMetadatas, targetPackage)
      }
    }
    return unprocessable
  }
}
