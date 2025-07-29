package com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger;

import com.geekinasuit.daggergrpc.api.ApplicationScope;
import com.geekinasuit.daggergrpc.api.GrpcApplication;
import com.geekinasuit.daggergrpc.api.GrpcCallScope;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.ExampleServer;
import dagger.Component;
import dagger.Provides;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Named;

@GrpcApplication(callScopedModules = {ApplicationGraph.CallModule.class})
@Component(modules = {ApplicationGraph.ApplicationModule.class, Grpc.Module.class})
@ApplicationScope
public interface ApplicationGraph extends Grpc.CallScopeGraph.Supplier {
  ExampleServer server();

  @Component.Builder
  interface Builder {
    ApplicationGraph build();
  }

  static ApplicationGraph.Builder builder() {
    return DaggerApplicationGraph.builder();
  }

  /** A module containing bindings/provisions of application-scoped (singleton) objects. */
  @dagger.Module
  interface ApplicationModule {
    @Provides
    @ApplicationScope
    @Named("app")
    static String someString() {
      return "blah";
    }
  }

  /** A module containing bindings/provisions of per-call-scoped objects. */
  @dagger.Module
  interface CallModule {
    AtomicInteger COUNTER = new AtomicInteger();

    @Provides
    @Named("call")
    @GrpcCallScope
    static Integer someInt() {
      return COUNTER.getAndIncrement();
    }
  }
}
