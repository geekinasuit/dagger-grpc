package com.geekinasuit.kspbridge.apt

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Nullability
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

class APTType(
  private val element: TypeElement,
  private val mirror: TypeMirror,
  override val arguments: List<KSTypeArgument> = listOf(),
) : KSType {
  constructor(type: DeclaredType) : this(type.asElement() as TypeElement, type)

  constructor(
    element: TypeElement,
    mirror: TypeMirror,
    vararg arguments: KSTypeArgument,
  ) : this(element, mirror, arguments.asList())

  override val annotations: Sequence<KSAnnotation>
    get() = TODO("Not yet implemented")

  override val declaration: KSDeclaration by lazy { APTClassDeclaration(element) }

  override val isError: Boolean
    get() = TODO("Not yet implemented")

  override val isFunctionType: Boolean
    get() = TODO("Not yet implemented")

  override val isMarkedNullable: Boolean
    get() = TODO("Not yet implemented")

  override val isSuspendFunctionType: Boolean
    get() = TODO("Not yet implemented")

  override val nullability: Nullability
    get() = TODO("Not yet implemented")

  override fun isAssignableFrom(that: KSType): Boolean {
    TODO("Not yet implemented")
  }

  override fun isCovarianceFlexible(): Boolean {
    TODO("Not yet implemented")
  }

  override fun isMutabilityFlexible(): Boolean {
    TODO("Not yet implemented")
  }

  override fun makeNotNullable(): KSType {
    TODO("Not yet implemented")
  }

  override fun makeNullable(): KSType {
    TODO("Not yet implemented")
  }

  override fun replace(arguments: List<KSTypeArgument>): KSType {
    TODO("Not yet implemented")
  }

  override fun starProjection(): KSType {
    TODO("Not yet implemented")
  }

  override fun toString() = "${this::class.simpleName}[${element.qualifiedName}]"
}
