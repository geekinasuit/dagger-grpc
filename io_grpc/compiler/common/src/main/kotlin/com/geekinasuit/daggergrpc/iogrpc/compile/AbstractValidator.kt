package com.geekinasuit.daggergrpc.iogrpc.compile

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import kotlin.reflect.KClass

abstract class AbstractValidator(protected val logger: KSPLogger) {
  protected fun getAnnotation(clazz: KSClassDeclaration, annotationType: KClass<*>): KSAnnotation? {
    val annotation =
      clazz.annotations.firstOrNull { it.shortName.asString() == annotationType.simpleName }
    return annotation.also {
      if (it == null) logger.error("No GrpcServiceHandler annotation found.", clazz)
    }
  }
}
