package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.kspbridge.apt.APTLogger
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.Filer
import javax.annotation.processing.Generated
import javax.lang.model.element.Modifier

class ModuleGenerator(val logger: APTLogger, val filer: Filer) {
  fun generateModule(handlers: List<HandlerMetadata>, targetPackage: String) {
    val graphClassName = ClassName.get(targetPackage, "GrpcCallScopeGraph")
    val graphModuleClassName = ClassName.get(targetPackage, "GrpcCallScopeGraphModule")
    val factoryClassName = ClassName.get(targetPackage, "GrpcCallScopeGraph", "Factory")
    val subcomponentClassName = ClassName.get("dagger", "Subcomponent")
    val subcomponentFactoryClassName = ClassName.get("dagger", "Subcomponent", "Factory")
    val moduleClassName = ClassName.get("dagger", "Module")
    val providesClassName = ClassName.get("dagger", "Provides")
    val intoSetClassName = ClassName.get("dagger.multibindings", "IntoSet")
    val bindableServiceClassName = ClassName.get("io.grpc", "BindableService")
    val grpcCallScopeClassName = ClassName.get("com.geekinasuit.daggergrpc.api", "GrpcCallScope")
    val grpcCallContextModuleClassName =
      ClassName.get("com.geekinasuit.daggergrpc.api", "GrpcCallContext", "Module")

    val generatedAnnotation =
      AnnotationSpec.builder(Generated::class.java)
        .addMember("value", "\$S", DaggerGrpcAPTProcessor::class.qualifiedName)
        .build()

    // GrpcCallScopeGraph
    val factoryInterface =
      TypeSpec.interfaceBuilder("Factory")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addAnnotation(AnnotationSpec.builder(subcomponentFactoryClassName).build())
        .addMethod(
          MethodSpec.methodBuilder("create")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(graphClassName)
            .build()
        )
        .build()

    val graphTypeBuilder =
      TypeSpec.interfaceBuilder("GrpcCallScopeGraph")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(generatedAnnotation)
        .addAnnotation(
          AnnotationSpec.builder(subcomponentClassName)
            .addMember("modules", "{\$T.class}", graphModuleClassName)
            .build()
        )
        .addAnnotation(AnnotationSpec.builder(grpcCallScopeClassName).build())

    for (md in handlers) {
      val handlerClassName = ClassName.get(md.packageName, md.name)
      graphTypeBuilder.addMethod(
        MethodSpec.methodBuilder(md.provisionName)
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .returns(handlerClassName)
          .build()
      )
    }
    graphTypeBuilder.addType(factoryInterface)

    JavaFile.builder(targetPackage, graphTypeBuilder.build()).build().writeTo(filer)

    // GrpcCallScopeGraphModule
    JavaFile.builder(
        targetPackage,
        TypeSpec.interfaceBuilder("GrpcCallScopeGraphModule")
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(generatedAnnotation)
          .addAnnotation(
            AnnotationSpec.builder(moduleClassName)
              .addMember("includes", "{\$T.class}", grpcCallContextModuleClassName)
              .addMember("subcomponents", "{\$T.class}", graphClassName)
              .build()
          )
          .build(),
      )
      .build()
      .writeTo(filer)

    // GrpcHandlersModule
    val handlersModuleBuilder =
      TypeSpec.classBuilder("GrpcHandlersModule")
        .addModifiers(Modifier.ABSTRACT)
        .addAnnotation(generatedAnnotation)
        .addAnnotation(
          AnnotationSpec.builder(moduleClassName)
            .addMember("includes", "{\$T.class}", graphModuleClassName)
            .build()
        )

    for (md in handlers) {
      val adapterClassName = ClassName.get(md.packageName, md.adapterName)
      handlersModuleBuilder.addMethod(
        MethodSpec.methodBuilder("${md.provisionName}Handler")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .addAnnotation(AnnotationSpec.builder(providesClassName).build())
          .addAnnotation(AnnotationSpec.builder(intoSetClassName).build())
          .addParameter(factoryClassName, "factory")
          .returns(bindableServiceClassName)
          .addStatement(
            "return new \$T(() -> factory.create().\$L())",
            adapterClassName,
            md.provisionName,
          )
          .build()
      )
    }

    JavaFile.builder(targetPackage, handlersModuleBuilder.build()).build().writeTo(filer)
  }
}
