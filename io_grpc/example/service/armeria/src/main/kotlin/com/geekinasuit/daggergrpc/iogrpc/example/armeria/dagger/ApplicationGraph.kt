package com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger

import com.geekinasuit.daggergrpc.api.ApplicationScope
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.ExampleServer
import dagger.Component

@Component(modules = [ApplicationGraphModule::class, GrpcHandlersModule::class])
@ApplicationScope
interface ApplicationGraph : GrpcCallScopeGraph.Supplier {
  fun server(): ExampleServer

  @Component.Builder
  interface Builder {
    fun build(): ApplicationGraph
  }

  companion object {
    fun builder(): Builder = DaggerApplicationGraph.builder()
  }
}
