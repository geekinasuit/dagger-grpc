package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.kspbridge.apt.APTClassDeclaration
import com.geekinasuit.kspbridge.apt.APTTypeArgument
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.javapoet.ClassName

fun KSTypeReference.toClassName(): ClassName = resolve().toClassName()

fun KSType.toClassName(): ClassName = ClassName.bestGuess(declaration.qualifiedName!!.asString())

fun KSClassDeclaration.toClassName(): ClassName =
  this.asType((this as APTClassDeclaration).element.typeParameters.map(::APTTypeArgument))
    .toClassName()
