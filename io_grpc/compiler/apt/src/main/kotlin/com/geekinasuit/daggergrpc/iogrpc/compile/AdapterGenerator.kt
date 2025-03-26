package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.kspbridge.apt.APTClassDeclaration
import com.geekinasuit.kspbridge.apt.APTLogger
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import io.grpc.ServerServiceDefinition
import java.util.concurrent.Callable
import javax.annotation.processing.Filer
import javax.annotation.processing.Generated
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Modifier

class AdapterGenerator(val env: RoundEnvironment, val logger: APTLogger, val filer: Filer) {
  fun generateAdapter(md: HandlerMetadata) {
    val file = doGenerateAdapter(md)
    logger.info(file.writeToString())
    file.writeTo(filer)
  }

  fun doGenerateAdapter(md: HandlerMetadata): JavaFile {
    logger.info("Generating adapter ${md.adapterName}")
    logger.info(md.toString())

    val serviceInterfaceProviderSpec =
      ParameterizedTypeName.get(
        ClassName.get(Callable::class.java),
        ClassName.bestGuess(md.serviceInterface.qualifiedName!!.asString()),
      )
    val serviceField =
      FieldSpec.builder(serviceInterfaceProviderSpec, "service", Modifier.PRIVATE, Modifier.FINAL)
        .build()

    val constructor =
      MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(serviceInterfaceProviderSpec, "service")
        .addStatement("this.service = service")
        .build()
    val typeBuilder =
      TypeSpec.classBuilder(md.adapterName)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterfaces(
          listOf(ClassName.bestGuess("io.grpc.BindableService"), md.serviceInterface.toClassName())
        )
        .addField(serviceField)
        .addAnnotation(
          AnnotationSpec.builder(Generated::class.java)
            .addMember("value", "\"${DaggerGrpcAPTProcessor::class.qualifiedName}\"")
            .build()
        )
        .addMethod(constructor)

    logger.info("Service interface: ${md.serviceInterface}")
    md.serviceInterface
      .getAllFunctions()
      .filter {
        when (it.simpleName.asString()) {
          "equals" -> false
          "hashCode" -> false
          "toString" -> false
          else -> true
        }
      }
      .forEach { fn ->
        val spec = MethodSpec.methodBuilder(fn.simpleName.asString()).addModifiers(Modifier.PUBLIC)
        for (parm in fn.parameters) {
          spec.addParameter((parm.type.toClassName()), parm.name!!.asString())
        }
        spec.addAnnotation(Override::class.java)
        spec.addStatement(
          """
            try {
              service.call().${"$"}L(${"$"}L);
            } catch (Exception e) {
              throw new java.lang.RuntimeException(e);
            }
          """
            .trimIndent(),
          fn.simpleName.asString(),
          fn.parameters.joinToString(",") { it.name!!.getShortName() },
        )
        typeBuilder.addMethod(spec.build())
      }
    typeBuilder.addMethod(
      MethodSpec.methodBuilder("bindService")
        .addModifiers(Modifier.PUBLIC)
        .returns(ServerServiceDefinition::class.java)
        .addStatement(
          "return \$T.bindService(this)",
          (md.grpcClass as APTClassDeclaration).toClassName(),
        )
        .addAnnotation(Override::class.java)
        .build()
    )
    val fileBuilder = JavaFile.builder(md.packageName, typeBuilder.build())
    /*
      class WhateverServiceAdapter2(val service: () -> AsyncService) : BindableService, AsyncService {
        override fun whatever(req: WhateverRequest, resp: StreamObserver<WhateverResponse>) =
          service().whatever(req, resp)

        override fun bindService(): ServerServiceDefinition = WhateverServiceGrpc.bindService(this)
      }
      }
    */
    return fileBuilder.build()
  }
}
