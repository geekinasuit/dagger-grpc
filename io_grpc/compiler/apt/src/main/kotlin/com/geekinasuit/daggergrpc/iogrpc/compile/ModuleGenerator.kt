package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.daggergrpc.api.GrpcCallContext
import com.geekinasuit.daggergrpc.api.GrpcCallScope
import com.geekinasuit.kspbridge.apt.APTClassDeclaration
import com.geekinasuit.kspbridge.apt.APTLogger
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoSet
import io.grpc.BindableService
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Modifier

class ModuleGenerator(env: RoundEnvironment, logger: APTLogger, filer: Filer) :
  Generator<ApplicationMetadata>(env, logger, filer) {

  override fun generate(input: ApplicationMetadata) {
    val file = doGenerateModule(input)
    logger.info(file.writeToString())
    file.writeTo(filer)
  }

  fun doGenerateModule(root: ApplicationMetadata): JavaFile =
    with(root) {
      val appGraph = clazz as APTClassDeclaration
      logger.info("Generating module for handlers ${handlers.joinToString { it.name }}")

      val wrapperClassName = ClassName.get(packageName, "Grpc")
      val wrapperClass =
        TypeSpec.interfaceBuilder(wrapperClassName)
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(generatedAnnotation())
      val subcomponentName = ClassName.get(packageName, "Grpc", "CallScopeGraph")
      val subcomponent =
        TypeSpec.interfaceBuilder(subcomponentName)
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .addAnnotation(subcomponentAnnotationWithModules(callScopedModules, handlers))
          .addAnnotation(AnnotationSpec.builder(GrpcCallScope::class.java).build())

      val subcomponentSupplierName =
        ClassName.get(packageName, "Grpc", "CallScopeGraph", "Supplier")
      val subcomponentSupplier =
        TypeSpec.interfaceBuilder(subcomponentSupplierName)
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .addMethod(
            MethodSpec.methodBuilder("callScope")
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .returns(subcomponentName)
              .build()
          )
      subcomponent.addType(subcomponentSupplier.build())

      val module =
        TypeSpec.classBuilder("Module")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.ABSTRACT)
          .addAnnotation(Module::class.java)
          .addMethod(
            MethodSpec.methodBuilder("callScopeGraphSupplier")
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addAnnotation(Binds::class.java)
              .addParameter(ClassName.get(appGraph.element), "impl")
              .returns(subcomponentSupplierName)
              .build()
          )

      handlers.forEach { handler ->
        val handlerMethodName = handler.name.lowerFirst()
        subcomponent.addMethod(
          MethodSpec.methodBuilder(handlerMethodName)
            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
            .returns(ClassName.bestGuess(handler.qualifiedName))
            .build()
        )
        module.addMethod(
          MethodSpec.methodBuilder("provide${handler.name}")
            .addAnnotation(Provides::class.java)
            .addAnnotation(IntoSet::class.java)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(subcomponentSupplierName, "supplier")
            .returns(BindableService::class.java)
            .addStatement(
              "return new \$T(() -> supplier.callScope().$handlerMethodName())",
              ClassName.get(handler.packageName, handler.adapterName),
            )
            .build()
        )
      }

      wrapperClass.addType(subcomponent.build())
      wrapperClass.addType(module.build())

      val fileBuilder = JavaFile.builder(root.packageName, wrapperClass.build())

      return fileBuilder.build()
    }

  private fun subcomponentAnnotationWithModules(
    callScopeModules: List<KSTypeReference>,
    handlers: List<HandlerMetadata>,
  ): AnnotationSpec? {
    // TODO(cgruber): Add in any additional modules obtained from the handlers
    val modules =
      listOf(ClassName.get(GrpcCallContext.Module::class.java)) +
        callScopeModules.map { it.toClassName() }
    val format = modules.joinToString(prefix = "{", postfix = "}") { "\$L" }
    val subcomponentAnnotation =
      AnnotationSpec.builder(Subcomponent::class.java)
        .addMember("modules", "{ \$T.class }", ClassName.get(GrpcCallContext.Module::class.java))
        .build()
    return subcomponentAnnotation
  }
}
