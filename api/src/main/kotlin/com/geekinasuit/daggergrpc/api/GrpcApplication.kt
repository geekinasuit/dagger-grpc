package com.geekinasuit.daggergrpc.api

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class GrpcApplication(
  /**
   * An optional array of module classes, all of which should be scope-restricted
   * to @[GrpcCallScope]. These will be appended to the generated call-scoped module list in the
   * generated @[dagger.Subcomponent]
   */
  val callScopedModules: Array<KClass<*>> = []
)
