package com.geekinasuit.daggergrpc.iogrpc.compile

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import javax.annotation.processing.Generated

fun SymbolProcessorEnvironment.generateModule(handlers: List<HandlerMetadata>, targetPackage: String) {
  val graphClassName = ClassName(targetPackage, "GrpcCallScopeGraph")
  val graphModuleClassName = ClassName(targetPackage, "GrpcCallScopeGraphModule")
  val factoryClassName = ClassName(targetPackage, "GrpcCallScopeGraph", "Factory")
  val subcomponentClassName = ClassName("dagger", "Subcomponent")
  val subcomponentFactoryClassName = ClassName("dagger", "Subcomponent", "Factory")
  val moduleClassName = ClassName("dagger", "Module")
  val providesClassName = ClassName("dagger", "Provides")
  val intoSetClassName = ClassName("dagger.multibindings", "IntoSet")
  val bindableServiceClassName = ClassName("io.grpc", "BindableService")
  val grpcCallScopeClassName = ClassName("com.geekinasuit.daggergrpc.api", "GrpcCallScope")
  val grpcCallContextModuleClassName =
    ClassName("com.geekinasuit.daggergrpc.api", "GrpcCallContext", "Module")

  val generatedAnnotation =
    AnnotationSpec.builder(Generated::class)
      .addMember("\"${DaggerGrpcSymbolProcessor::class.qualifiedName}\"")
      .build()

  val dependencies =
    Dependencies(true, *handlers.mapNotNull { it.ast.containingFile }.toTypedArray())

  // GrpcCallScopeGraph
  val factoryInterface =
    TypeSpec.interfaceBuilder("Factory")
      .addAnnotation(AnnotationSpec.builder(subcomponentFactoryClassName).build())
      .addFunction(FunSpec.builder("create").returns(graphClassName).build())
      .build()

  val graphTypeBuilder =
    TypeSpec.interfaceBuilder("GrpcCallScopeGraph")
      .addAnnotation(generatedAnnotation)
      .addAnnotation(
        AnnotationSpec.builder(subcomponentClassName)
          .addMember("modules = [%T::class]", graphModuleClassName)
          .build()
      )
      .addAnnotation(AnnotationSpec.builder(grpcCallScopeClassName).build())

  for (md in handlers) {
    graphTypeBuilder.addFunction(
      FunSpec.builder(md.provisionName).returns(md.ast.toClassName()).build()
    )
  }
  graphTypeBuilder.addType(factoryInterface)

  FileSpec.builder(targetPackage, "GrpcCallScopeGraph")
    .addType(graphTypeBuilder.build())
    .build()
    .writeTo(codeGenerator, dependencies)

  // GrpcCallScopeGraphModule
  FileSpec.builder(targetPackage, "GrpcCallScopeGraphModule")
    .addType(
      TypeSpec.objectBuilder("GrpcCallScopeGraphModule")
        .addAnnotation(generatedAnnotation)
        .addAnnotation(
          AnnotationSpec.builder(moduleClassName)
            .addMember("includes = [%T::class]", grpcCallContextModuleClassName)
            .addMember("subcomponents = [%T::class]", graphClassName)
            .build()
        )
        .build()
    )
    .build()
    .writeTo(codeGenerator, dependencies)

  // GrpcHandlersModule
  val handlersModuleBuilder =
    TypeSpec.objectBuilder("GrpcHandlersModule")
      .addAnnotation(generatedAnnotation)
      .addAnnotation(
        AnnotationSpec.builder(moduleClassName)
          .addMember("includes = [%T::class]", graphModuleClassName)
          .build()
      )

  for (md in handlers) {
    val adapterClassName = ClassName(md.packageName, md.adapterName)
    handlersModuleBuilder.addFunction(
      FunSpec.builder("${md.provisionName}Handler")
        .addAnnotation(AnnotationSpec.builder(providesClassName).build())
        .addAnnotation(AnnotationSpec.builder(intoSetClassName).build())
        .addParameter("factory", factoryClassName)
        .returns(bindableServiceClassName)
        .addStatement("return %T { factory.create().%L() }", adapterClassName, md.provisionName)
        .build()
    )
  }

  FileSpec.builder(targetPackage, "GrpcHandlersModule")
    .addType(handlersModuleBuilder.build())
    .build()
    .writeTo(codeGenerator, dependencies)
}
