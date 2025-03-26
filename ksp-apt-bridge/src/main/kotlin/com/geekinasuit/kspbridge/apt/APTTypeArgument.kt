package com.geekinasuit.kspbridge.apt

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Origin
import com.google.devtools.ksp.symbol.Variance
import javax.lang.model.element.Element

class APTTypeArgument(override val element: Element) : KSTypeArgument, APTNode {

  override val annotations: Sequence<KSAnnotation>
    get() = TODO("Not yet implemented")

  override val location: Location
    get() = TODO("Not yet implemented")

  override val origin: Origin
    get() = TODO("Not yet implemented")

  override val parent: KSNode?
    get() = TODO("Not yet implemented")

  override val type: KSTypeReference?
    get() = TODO("Not yet implemented")

  override val variance: Variance
    get() = TODO("Not yet implemented")

  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D) =
    visitor.visitTypeArgument(this, data)
}
