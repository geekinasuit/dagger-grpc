package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.daggergrpc.api.GrpcServiceHandler
import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class DaggerGrpcAPTProcessor : AbstractProcessor() {
  override fun getSupportedAnnotationTypes(): MutableSet<String> =
    mutableSetOf(GrpcServiceHandler::class.toString())

  private lateinit var logger: ASTLogger

  @Synchronized
  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    logger = ASTLogger(processingEnv.messager)
  }

  override fun process(
    annotations: MutableSet<out TypeElement>,
    roundEnv: RoundEnvironment,
  ): Boolean {
    val validator = Validator(logger)
    val elements =
      annotations
        .asSequence()
        .flatMap(roundEnv::getElementsAnnotatedWith)
        .onEach {
          if (it.kind != ElementKind.CLASS)
            logger.error("GrpcServiceHandler must only be used on a class")
        }
        .filter { it.kind == ElementKind.CLASS }
        .filterIsInstance<TypeElement>()
        .map(::ASTClassDeclaration)
    // .onEach(validator::validate)

    return true
  }
}
