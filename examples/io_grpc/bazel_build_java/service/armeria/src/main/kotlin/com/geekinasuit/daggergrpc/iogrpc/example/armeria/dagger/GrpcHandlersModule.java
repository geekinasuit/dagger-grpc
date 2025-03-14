package com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger;

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.services.HelloWorldServiceAdapter;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.services.WhateverServiceAdapter;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;

@Module
abstract class GrpcHandlersModule {
  @Provides
  @IntoSet
  public static BindableService helloWorldHandler(GrpcCallScopeGraph.Supplier supplier) {
    return new HelloWorldServiceAdapter(() -> supplier.callScope().helloWorld());
  }

  @Provides
  @IntoSet
  public static BindableService whateverHandler(GrpcCallScopeGraph.Supplier supplier) {
    return new WhateverServiceAdapter(() -> supplier.callScope().whatever());
  }
}
