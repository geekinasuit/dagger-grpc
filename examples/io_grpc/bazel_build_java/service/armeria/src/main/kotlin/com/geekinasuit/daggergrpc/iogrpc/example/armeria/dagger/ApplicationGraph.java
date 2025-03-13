package com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger;

import com.geekinasuit.daggergrpc.api.ApplicationScope;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.ExampleServer;
import dagger.Component;

@Component(modules = {ApplicationGraphModule.class, GrpcHandlersModule.class})
@ApplicationScope
public interface ApplicationGraph extends GrpcCallScopeGraph.Supplier {
  ExampleServer server();

  @Component.Builder
  interface Builder {
    ApplicationGraph build();
  }

  public static Builder builder() {
    return DaggerApplicationGraph.builder();
  }
}
