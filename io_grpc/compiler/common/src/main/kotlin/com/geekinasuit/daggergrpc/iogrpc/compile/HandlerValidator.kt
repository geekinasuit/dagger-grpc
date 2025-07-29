package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.daggergrpc.api.GrpcServiceHandler
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

class HandlerValidator(logger: KSPLogger) : AbstractValidator(logger) {
  internal fun validate(clazz: KSClassDeclaration): HandlerMetadata? {
    val annotation = getAnnotation(clazz, GrpcServiceHandler::class) ?: return null
    val grpcClass = validateGrpcClass(annotation) ?: return null
    val serviceInterface = validateServiceInterface(grpcClass) ?: return null
    logger.info("Validating $clazz")
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
        if (it == null) logger.error("No AsyncService found in GRPC Wrapper type.", grpcClass)
      }
  }

  private fun validateGrpcClass(annotation: KSAnnotation): KSClassDeclaration? {
    val wrapperTypeParameter =
      annotation.arguments.firstOrNull { it.name!!.asString() == "grpcWrapperType" }
    return wrapperTypeParameter
      ?.value
      ?.let { (it as KSType).declaration }
      ?.let { it as KSClassDeclaration }
      .also { if (it == null) logger.error("grpcWrapperType was null.", wrapperTypeParameter) }
  }
}
