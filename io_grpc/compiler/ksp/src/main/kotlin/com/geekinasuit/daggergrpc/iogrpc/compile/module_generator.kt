package com.geekinasuit.daggergrpc.iogrpc.compile

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

fun SymbolProcessorEnvironment.generateModule(md: Sequence<HandlerMetadata>) {
  logger.info("Generating GrpcHandler")
  logger.info(md.toString())
}
