package com.geekinasuit.daggergrpc.api

import kotlin.reflect.KClass

/** Marks a class as a gRPC call handler. */
@Target(AnnotationTarget.CLASS)
annotation class GrpcServiceHandler(
  /**
   * The generated GRPC wrapper type (e.g. FooGrpc) which contains the AsyncAdapter for the service.
   */
  val grpcWrapperType: KClass<*>,
  /**
   * An optional array of module classes, all of which should be scope-restricted
   * to @[GrpcCallScope]. These will be appended to the generated call-scoped module list in
   * the @[dagger.Subcomponent]
   */
  val callScopedModules: Array<KClass<*>> = [],
)
