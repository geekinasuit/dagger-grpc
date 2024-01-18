package com.geekinasuit.daggergrpc.armeria

import com.linecorp.armeria.server.grpc.GrpcService
import io.grpc.BindableService
import io.grpc.ServerInterceptor

fun wrapService(
  bindableService: BindableService,
  vararg interceptors: ServerInterceptor,
): GrpcService {
  return GrpcService.builder().addService(bindableService).intercept(*interceptors).build()
}
