package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.daggergrpc.api.GrpcApplication
import com.geekinasuit.kspbridge.apt.APTTypeReference
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import javax.lang.model.element.AnnotationValue
import javax.lang.model.type.DeclaredType

class ApplicationValidator(logger: KSPLogger, val handlers: List<HandlerMetadata>) :
  AbstractValidator(logger) {
  // TODO(cgruber): Factor this properly so it can be done in both APT and KSP.

  internal fun validate(clazz: KSClassDeclaration): ApplicationMetadata? {

    val annotation = getAnnotation(clazz, GrpcApplication::class) ?: return null
    val callScopedModulesArgument =
      annotation.arguments
        .firstOrNull { it.name?.asString() == "callScopedModules" }
        ?.value
        ?.let { it as List<AnnotationValue> } ?: listOf()
    val typeReferences =
      callScopedModulesArgument.map { APTTypeReference(it.value as DeclaredType) }
    return ApplicationMetadata(
      packageName = clazz.packageName.asString(),
      handlers = handlers,
      clazz = clazz,
      callScopedModules = typeReferences,
    )
  }
}
