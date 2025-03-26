package com.geekinasuit.kspbridge.apt

import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunction
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin
import javax.lang.model.element.ExecutableElement

class APTFunctionDeclaration(override val element: ExecutableElement) :
  KSFunctionDeclaration, APTNode {
  override val annotations: Sequence<KSAnnotation>
    get() = TODO("Not yet implemented")

  override val containingFile: KSFile?
    get() = TODO("Not yet implemented")

  override val declarations: Sequence<KSDeclaration>
    get() = TODO("Not yet implemented")

  override val docString: String?
    get() = TODO("Not yet implemented")

  override val extensionReceiver: KSTypeReference?
    get() = TODO("Not yet implemented")

  override val functionKind: FunctionKind
    get() = TODO("Not yet implemented")

  override val isAbstract: Boolean
    get() = TODO("Not yet implemented")

  override val isActual: Boolean
    get() = TODO("Not yet implemented")

  override val isExpect: Boolean
    get() = TODO("Not yet implemented")

  override val location: Location
    get() = TODO("Not yet implemented")

  override val modifiers: Set<Modifier>
    get() = TODO("Not yet implemented")

  override val origin: Origin
    get() = TODO("Not yet implemented")

  override val packageName: KSName
    get() = TODO("Not yet implemented")

  override val parameters: List<KSValueParameter> by lazy {
    element.parameters.map(::APTValueParameter)
  }

  override val parent: KSNode?
    get() = TODO("Not yet implemented")

  override val parentDeclaration: KSDeclaration?
    get() = TODO("Not yet implemented")

  override val qualifiedName: KSName?
    get() = TODO("Not yet implemented")

  override val returnType: KSTypeReference?
    get() = TODO("Not yet implemented")

  override val simpleName: KSName by lazy { APTName(element.simpleName) }

  override val typeParameters: List<KSTypeParameter>
    get() = TODO("Not yet implemented")

  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D) =
    visitor.visitFunctionDeclaration(this, data)

  override fun asMemberOf(containing: KSType): KSFunction {
    TODO("Not yet implemented")
  }

  override fun findActuals(): Sequence<KSDeclaration> {
    TODO("Not yet implemented")
  }

  override fun findExpects(): Sequence<KSDeclaration> {
    TODO("Not yet implemented")
  }

  override fun findOverridee(): KSDeclaration? {
    TODO("Not yet implemented")
  }

  override fun toString() =
    "${this::class.simpleName}[${element.simpleName}(${
    element.parameters.joinToString(", ") { it.asType().toString() }
  })]"
}
