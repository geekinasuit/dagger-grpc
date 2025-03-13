package com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger;

import dagger.Binds;
import dagger.Module;

@Module
public interface ApplicationGraphModule {
  @Binds
  GrpcCallScopeGraph.Supplier callGraphSupplier(ApplicationGraph impl);
}
