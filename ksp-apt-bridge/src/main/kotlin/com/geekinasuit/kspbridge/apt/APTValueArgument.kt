package com.geekinasuit.kspbridge.apt

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Origin
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

class APTValueArgument(
  private val nameElement: ExecutableElement,
  private val annotationValue: AnnotationValue,
) : KSValueArgument {

  override val annotations: Sequence<KSAnnotation>
    get() = TODO("Not yet implemented")

  override val isSpread: Boolean
    get() = TODO("Not yet implemented")

  override val location: Location
    get() = TODO("Not yet implemented")

  override val name: KSName by lazy { APTName(nameElement.simpleName) }

  override val origin: Origin
    get() = TODO("Not yet implemented")

  override val parent: KSNode?
    get() = TODO("Not yet implemented")

  override val value: Any? by lazy {
    when (val value = annotationValue.value) {
      is TypeMirror -> APTType(value as DeclaredType)
      null -> null
      else -> value
    }
  }

  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D) =
    visitor.visitValueArgument(this, data)
}
