package com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger

import com.geekinasuit.daggergrpc.api.GrpcCallScope
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.services.HelloWorldService
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.services.WhateverService
import dagger.Subcomponent
import javax.annotation.Generated

@Generated("to be generated")
@Subcomponent(modules = [GrpcCallScopeGraphModule::class])
@GrpcCallScope
interface GrpcCallScopeGraph {
  fun helloWorld(): HelloWorldService

  fun whatever(): WhateverService

  interface Supplier {
    fun callScope(): GrpcCallScopeGraph
  }
}
