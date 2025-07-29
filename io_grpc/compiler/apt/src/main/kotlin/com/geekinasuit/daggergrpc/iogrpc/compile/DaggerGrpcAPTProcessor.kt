package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.daggergrpc.api.GrpcApplication
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
    mutableSetOf(
      GrpcServiceHandler::class.java.canonicalName,
      GrpcApplication::class.java.canonicalName,
    )

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
    val handlerValidator = HandlerValidator(logger)
    val elements = roundEnv.getElementsAnnotatedWith(GrpcServiceHandler::class.java)
    val adapterGenerator = AdapterGenerator(roundEnv, logger, processingEnv.filer)
    val moduleGenerator = ModuleGenerator(roundEnv, logger, processingEnv.filer)
    val handlerMetadatas =
      elements
        .onEach {
          if (it.kind != ElementKind.CLASS)
            logger.error("GrpcServiceHandler must only be used on a class")
        }
        .filter { it.kind == ElementKind.CLASS }
        .filterIsInstance<TypeElement>()
        .map(::APTClassDeclaration)
        .mapNotNull(handlerValidator::validate)
        .onEach(adapterGenerator::generate)
        .toList()

    if (handlerMetadatas.isEmpty()) {
      logger.warn("No valid classes were annotated with @GrpcServiceHandler")
    } else {
      val appValidator = ApplicationValidator(logger, handlerMetadatas)
      val appMetadata =
        roundEnv
          .getElementsAnnotatedWith(GrpcApplication::class.java)
          .asSequence()
          .filterIsInstance<TypeElement>()
          .map(::APTClassDeclaration)
          .firstOrNull()
          ?.let(appValidator::validate)
          ?.also(moduleGenerator::generate)
          ?: run {
            return true
          }
    }
    return true
  }
}
