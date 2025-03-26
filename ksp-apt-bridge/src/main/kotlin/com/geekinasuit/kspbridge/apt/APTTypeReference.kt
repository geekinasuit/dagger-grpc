package com.geekinasuit.kspbridge.apt

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin
import javax.lang.model.type.DeclaredType

class APTTypeReference(private val mirror: DeclaredType) : KSTypeReference {
  override val annotations: Sequence<KSAnnotation>
    get() = TODO("Not yet implemented")

  override val element: KSReferenceElement?
    get() = TODO("Not yet implemented")

  override val location: Location
    get() = TODO("Not yet implemented")

  override val modifiers: Set<Modifier>
    get() = TODO("Not yet implemented")

  override val origin: Origin
    get() = TODO("Not yet implemented")

  override val parent: KSNode?
    get() = TODO("Not yet implemented")

  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
    TODO("Not yet implemented")
  }

  override fun resolve(): KSType = APTType(mirror)
}
