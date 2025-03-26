package com.geekinasuit.kspbridge.apt

import com.google.devtools.ksp.symbol.AnnotationUseSiteTarget
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Origin
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement

class APTAnnotation(private val mirror: AnnotationMirror) : KSAnnotation {
  val type = mirror.annotationType.asElement() as TypeElement
  override val annotationType: KSTypeReference = APTTypeReference(mirror.annotationType)

  override val arguments: List<KSValueArgument> by lazy {
    mirror.elementValues.map { (k, v) -> APTValueArgument(k, v) }
  }

  override val defaultArguments: List<KSValueArgument>
    get() = TODO("Not yet implemented")

  override val location: Location
    get() = TODO("Not yet implemented")

  override val origin: Origin
    get() = TODO("Not yet implemented")

  override val parent: KSNode?
    get() = TODO("Not yet implemented")

  override val shortName: KSName by lazy { APTName(type.simpleName) }

  override val useSiteTarget: AnnotationUseSiteTarget?
    get() = TODO("Not yet implemented")

  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D) =
    visitor.visitAnnotation(this, data)

  override fun toString() = "${this::class.simpleName}[${mirror}]"
}
