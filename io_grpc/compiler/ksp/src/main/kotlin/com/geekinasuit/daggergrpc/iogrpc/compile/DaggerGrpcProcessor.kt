package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.daggergrpc.api.GrpcServiceHandler
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

class DaggerGrpcProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
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

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val symbols = resolver.getSymbolsWithAnnotation(GrpcServiceHandler::class.qualifiedName!!)
    val unprocessable = mutableListOf<KSAnnotated>()
    val handlerMetadatas =
      symbols
        .onEach { env.logger.info("Processing $it") }
        .onEach { if (it !is KSClassDeclaration) unprocessable += it }
        .filterIsInstance<KSClassDeclaration>()
        .map(::validate)
        .filterNotNull()
        .onEach(env::generateAdapter)
    if (handlerMetadatas.toList().isEmpty()) {
      env.logger.warn("No valid classes were annotated with @GrpcServiceHandler")
    } else env.generateModule(handlerMetadatas)
    return unprocessable
  }

  private fun validate(clazz: KSClassDeclaration): HandlerMetadata? {
    val annotation = validateAnnotation(clazz) ?: return null
    val grpcClass = validateGrpcClass(annotation) ?: return null
    val serviceInterface = validateServiceInterface(grpcClass) ?: return null
    env.logger.info("Validating $clazz")
    return HandlerMetadata(
      ast = clazz,
      name = clazz.simpleName.asString(),
      annotation = annotation,
      packageName = clazz.packageName.asString(),
      grpcClass = grpcClass,
      serviceInterface = serviceInterface,
    )
  }

  private fun validateServiceInterface(grpcClass: KSClassDeclaration): KSClassDeclaration? {
    val svc = grpcClass.declarations.firstOrNull { it.simpleName.asString() == "AsyncService" }
    return svc
      ?.let { it as KSClassDeclaration }
      .also {
        if (it == null) env.logger.error("No AsyncService found in GRPC Wrapper type.", grpcClass)
      }
  }

  private fun validateGrpcClass(annotation: KSAnnotation): KSClassDeclaration? {
    val wrapperTypeParameter =
      annotation.arguments.firstOrNull { it.name!!.asString() == "grpcWrapperType" }
    return wrapperTypeParameter
      ?.value
      ?.let { (it as KSType).declaration }
      ?.let { it as KSClassDeclaration }
      .also { if (it == null) env.logger.error("grpcWrapperType was null.", wrapperTypeParameter) }
  }

  private fun validateAnnotation(clazz: KSClassDeclaration): KSAnnotation? {
    val annotation =
      clazz.annotations.firstOrNull {
        it.shortName.asString() == GrpcServiceHandler::class.simpleName
      }
    return annotation.also {
      if (it == null) env.logger.error("No GrpcServiceHandler annotation found.", clazz)
    }
  }
}
