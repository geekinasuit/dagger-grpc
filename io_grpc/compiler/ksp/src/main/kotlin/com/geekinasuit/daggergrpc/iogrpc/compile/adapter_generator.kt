package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcProcessor.HandlerMetadata
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import io.grpc.ServerServiceDefinition
import javax.annotation.processing.Generated

fun SymbolProcessorEnvironment.generateAdapter(md: HandlerMetadata) {
  val file = doGenerateAdapter(md)
  logger.info(file.writeToString())
  val dependencies = Dependencies(true, md.ast.containingFile!!)
  file.writeTo(codeGenerator, dependencies)
}

fun SymbolProcessorEnvironment.doGenerateAdapter(md: HandlerMetadata): FileSpec {
  md.ast.getAllFunctions()
  logger.info("Generating adapter ${md.adapterName}")
  logger.info(md.toString())
  val serviceInterfaceProviderSpec =
    LambdaTypeName.get(null, listOf(), md.serviceInterface.toClassName())
  val serviceProperty =
    PropertySpec.builder("service", serviceInterfaceProviderSpec, KModifier.PRIVATE)
      .initializer("service")
      .build()
  val typeBuilder =
    TypeSpec.classBuilder(md.adapterName)
      .addSuperinterfaces(
        listOf(ClassName.bestGuess("io.grpc.BindableService"), md.serviceInterface.toClassName())
      )
      .addAnnotation(
        AnnotationSpec.builder(Generated::class)
          .addMember("\"${DaggerGrpcProcessor::class.qualifiedName}\"")
          .build()
      )
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameter(serviceProperty.name, serviceInterfaceProviderSpec)
          .build()
      )
      .addProperty(serviceProperty)

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
      val spec = FunSpec.builder(fn.simpleName.asString())
      for (parm in fn.parameters) {
        spec.addParameter(parm.name!!.asString(), parm.type.toTypeName())
      }
      spec.addModifiers(KModifier.OVERRIDE)
      spec.addStatement(
        "service().%L(%L)",
        fn.simpleName.asString(),
        fn.parameters.joinToString(",") { it.name!!.getShortName() },
      )
      typeBuilder.addFunction(spec.build())
    }
  typeBuilder.addFunction(
    FunSpec.builder("bindService")
      .returns(ServerServiceDefinition::class.asClassName())
      .addStatement("return %T.bindService(this)", md.grpcClass.toClassName())
      .addModifiers(KModifier.OVERRIDE)
      .build()
  )
  val fileBuilder = FileSpec.builder(md.packageName, md.adapterName).addType(typeBuilder.build())
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
