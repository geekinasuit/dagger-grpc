package com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger

import com.geekinasuit.daggergrpc.api.GrpcCallScope
import javax.annotation.Generated
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.HelloWorldService
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.WhateverService
import dagger.Subcomponent

@Generated
@Subcomponent(modules = [GrpcCallScopeGraphModule::class])
@GrpcCallScope
interface GrpcCallScopeGraph {
  fun helloWorld(): HelloWorldService

  fun whatever(): WhateverService

  interface Supplier {
    fun callScope(): GrpcCallScopeGraph
  }
}

