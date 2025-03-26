package com.geekinasuit.kspbridge.apt

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Origin
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType

class APTValueParameter(private val element: VariableElement) : KSValueParameter {
  override val annotations: Sequence<KSAnnotation>
    get() = TODO("Not yet implemented")

  override val hasDefault: Boolean
    get() = TODO("Not yet implemented")

  override val isCrossInline: Boolean
    get() = TODO("Not yet implemented")

  override val isNoInline: Boolean
    get() = TODO("Not yet implemented")

  override val isVal: Boolean
    get() = TODO("Not yet implemented")

  override val isVar: Boolean
    get() = TODO("Not yet implemented")

  override val isVararg: Boolean
    get() = TODO("Not yet implemented")

  override val location: Location
    get() = TODO("Not yet implemented")

  override val name: KSName = APTName(element.simpleName)

  override val origin: Origin
    get() = TODO("Not yet implemented")

  override val parent: KSNode?
    get() = TODO("Not yet implemented")

  override val type: KSTypeReference
    get() = APTTypeReference(element.asType() as DeclaredType)

  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D) =
    visitor.visitValueParameter(this, data)
}
