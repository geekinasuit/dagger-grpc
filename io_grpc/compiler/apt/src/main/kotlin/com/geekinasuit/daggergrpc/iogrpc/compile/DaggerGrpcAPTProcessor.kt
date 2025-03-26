package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.daggergrpc.api.GrpcServiceHandler
import com.geekinasuit.kspbridge.apt.APTClassDeclaration
import com.geekinasuit.kspbridge.apt.APTLogger
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
    mutableSetOf(GrpcServiceHandler::class.java.canonicalName)

  private lateinit var logger: APTLogger

  @Synchronized
  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    logger = APTLogger(processingEnv.messager)
  }

  override fun process(
    annotations: MutableSet<out TypeElement>,
    roundEnv: RoundEnvironment,
  ): Boolean {
    logger.info("Beginning GrpcDaggerAPT Processor Run")
    val validator = Validator(logger)
    val elements = annotations.flatMap(roundEnv::getElementsAnnotatedWith)
    val adapterGenerator = AdapterGenerator(roundEnv, logger, processingEnv.filer)
    val handlerMetadatas =
      elements
        .onEach {
          if (it.kind != ElementKind.CLASS)
            logger.error("GrpcServiceHandler must only be used on a class")
        }
        .filter { it.kind == ElementKind.CLASS }
        .filterIsInstance<TypeElement>()
        .map(::APTClassDeclaration)
        .map(validator::validate)
        .filterNotNull()
        .onEach(adapterGenerator::generateAdapter)

    //    if (handlerMetadatas.toList().isEmpty()) {
    //      logger.warn("No valid classes were annotated with @GrpcServiceHandler")
    //    } else env.generateModule(handlerMetadatas)
    return true
  }
}
