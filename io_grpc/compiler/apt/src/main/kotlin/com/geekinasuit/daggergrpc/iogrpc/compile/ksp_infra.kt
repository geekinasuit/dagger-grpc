package com.geekinasuit.daggergrpc.iogrpc.compile

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import javax.annotation.processing.Messager
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.NOTE
import javax.tools.Diagnostic.Kind.OTHER
import javax.tools.Diagnostic.Kind.WARNING

/**
 * An implementation of KSPLogger which is backed by [javax.annotation.processing.Messager] from the
 * [javax.annotation.processing.ProcessingEnvironment]. If the KSNOde optionally supplied to a
 * logging method is implemented by [ASTNode] then the underlying element is plumbed through. (This
 * should be the only non-test usage pattern, but the contrary condition is captured, and then no
 * element is offered).
 */
class ASTLogger(val messager: Messager) : KSPLogger {
  override fun error(message: String, symbol: KSNode?) {
    val element = if (symbol is ASTNode) symbol.element else null
    messager.printMessage(ERROR, message, element)
  }

  override fun exception(e: Throwable) {
    messager.printMessage(ERROR, e.toString())
  }

  override fun info(message: String, symbol: KSNode?) {
    val element = if (symbol is ASTNode) symbol.element else null
    messager.printMessage(NOTE, message, element)
  }

  override fun logging(message: String, symbol: KSNode?) {
    val element = if (symbol is ASTNode) symbol.element else null
    messager.printMessage(OTHER, message, element)
  }

  override fun warn(message: String, symbol: KSNode?) {
    val element = if (symbol is ASTNode) symbol.element else null
    messager.printMessage(WARNING, message, element)
  }
}
