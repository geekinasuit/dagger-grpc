package com.geekinasuit.kspbridge.apt

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType

class APTClassDeclaration(
  override val element: TypeElement,
  private val mirror: DeclaredType,
  override val typeParameters: List<KSTypeParameter> = listOf(),
) : KSClassDeclaration, APTNode {
  constructor(type: TypeElement) : this(type, type.asType() as DeclaredType)

  override val annotations: Sequence<KSAnnotation>
    get() = element.annotationMirrors.map(::APTAnnotation).asSequence()

  override val classKind: ClassKind =
    when (element.kind) {
      ElementKind.ENUM -> ClassKind.ENUM_CLASS
      ElementKind.CLASS -> ClassKind.CLASS
      ElementKind.ANNOTATION_TYPE -> ClassKind.ANNOTATION_CLASS
      ElementKind.INTERFACE -> ClassKind.INTERFACE
      ElementKind.ENUM_CONSTANT -> ClassKind.ENUM_ENTRY
      else -> throw NotImplementedError("$element.kind is not supported as a KSP ClassKind")
    }

  override val containingFile: KSFile?
    get() = TODO("Not yet implemented")

  override val declarations: Sequence<KSDeclaration> by lazy {
    element.enclosedElements
      .map {
        when (it) {
          is TypeElement -> APTClassDeclaration(it)
          is VariableElement -> APTPropertyDeclaration(it)
          is ExecutableElement -> APTFunctionDeclaration(it)
          else -> throw UnsupportedOperationException("${it.kind} is not supported.")
        }
      }
      .asSequence()
  }

  override val docString: String?
    get() = TODO("Not yet implemented")

  override val isActual: Boolean
    get() = TODO("Not yet implemented")

  override val isCompanionObject: Boolean
    get() = TODO("Not yet implemented")

  override val isExpect: Boolean
    get() = TODO("Not yet implemented")

  override val location: Location
    get() = TODO("Not yet implemented")

  override val modifiers: Set<Modifier>
    get() = TODO("Not yet implemented")

  override val origin: Origin
    get() = TODO("Not yet implemented")

  override val packageName: KSName by lazy {
    val lastIndex = element.qualifiedName.lastIndexOf('.')
    if (lastIndex != -1) APTName(element.qualifiedName.substring(0, lastIndex)) else APTName("")
  }

  override val parent: KSNode?
    get() = TODO("Not yet implemented")

  override val parentDeclaration: KSDeclaration?
    get() = TODO("Not yet implemented")

  override val primaryConstructor: KSFunctionDeclaration?
    get() = TODO("Not yet implemented")

  override val qualifiedName: KSName? by lazy { APTName(element.qualifiedName) }

  override val simpleName: KSName by lazy { APTName(element.simpleName) }

  override val superTypes: Sequence<KSTypeReference>
    get() = TODO("Not yet implemented")

  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D) =
    visitor.visitClassDeclaration(this, data)

  override fun asStarProjectedType(): KSType =
    throw UnsupportedOperationException("Java types do not have star projection.")

  override fun asType(typeArguments: List<KSTypeArgument>): KSType =
    APTType(element, mirror, typeArguments)

  override fun findActuals(): Sequence<KSDeclaration> {
    TODO("Not yet implemented")
  }

  override fun findExpects(): Sequence<KSDeclaration> {
    TODO("Not yet implemented")
  }

  override fun getAllFunctions(): Sequence<KSFunctionDeclaration> =
    element.enclosedElements
      .filter { it.kind == ElementKind.METHOD }
      .filterIsInstance<ExecutableElement>()
      .map(::APTFunctionDeclaration)
      .asSequence()

  override fun getAllProperties(): Sequence<KSPropertyDeclaration> {
    TODO("Not yet implemented")
  }

  override fun getSealedSubclasses(): Sequence<KSClassDeclaration> {
    TODO("Not yet implemented")
  }

  override fun toString() = "${this::class.simpleName}[${element.qualifiedName}]"
}
