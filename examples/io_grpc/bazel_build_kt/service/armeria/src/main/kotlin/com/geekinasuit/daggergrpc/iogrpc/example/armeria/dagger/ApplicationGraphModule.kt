package com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger

import dagger.Module
import dagger.Provides

@Module
object ApplicationGraphModule {
  @Provides fun callGraphSupplier(impl: ApplicationGraph): GrpcCallScopeGraph.Supplier = impl
}
