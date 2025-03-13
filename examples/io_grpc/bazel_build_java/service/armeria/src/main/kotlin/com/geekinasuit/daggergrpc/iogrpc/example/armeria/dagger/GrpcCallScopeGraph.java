package com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger;

import com.geekinasuit.daggergrpc.api.GrpcCallScope;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.services.HelloWorldService;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.services.WhateverService;
import dagger.Subcomponent;
import javax.annotation.processing.Generated;

@Generated("to be generated")
@Subcomponent(modules = {GrpcCallScopeGraphModule.class})
@GrpcCallScope
public interface GrpcCallScopeGraph {
  HelloWorldService helloWorld();

  WhateverService whatever();

  interface Supplier {
    GrpcCallScopeGraph callScope();
  }
}
