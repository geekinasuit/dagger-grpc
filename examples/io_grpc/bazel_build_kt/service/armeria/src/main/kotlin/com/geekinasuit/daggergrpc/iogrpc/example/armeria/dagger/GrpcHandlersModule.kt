package com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.services.HelloWorldServiceAdapter
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.services.WhateverServiceAdapter
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.grpc.BindableService
import javax.annotation.Generated

@Generated("to be generated")
@Module
object GrpcHandlersModule {
  @Provides
  @IntoSet
  fun helloWorldHandler(supplier: GrpcCallScopeGraph.Supplier): BindableService =
    HelloWorldServiceAdapter {
      supplier.callScope().helloWorld()
    }

  @Provides
  @IntoSet
  fun whateverHandler(supplier: GrpcCallScopeGraph.Supplier): BindableService =
    WhateverServiceAdapter {
      supplier.callScope().whatever()
    }
}
