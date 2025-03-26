package com.geekinasuit.daggergrpc.iogrpc.compile

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class DaggerGrpcSymbolProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    DaggerGrpcSymbolProcessor(environment)
}
