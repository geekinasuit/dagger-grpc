package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc.AsyncService
import io.grpc.BindableService
import io.grpc.ServerServiceDefinition
import io.grpc.stub.StreamObserver

class HelloWorldServiceAdapter(val service: () -> AsyncService) : BindableService, AsyncService {
  override fun sayHello(
    request: HelloWorld.SayHelloRequest,
    responseObserver: StreamObserver<HelloWorld.SayHelloResponse>
  ) = service().sayHello(request, responseObserver)

  override fun sayGoodbye(
    request: HelloWorld.SayGoodbyeRequest,
    responseObserver: StreamObserver<HelloWorld.SayGoodbyeResponse>
  ) = service().sayGoodbye(request, responseObserver)

  override fun bindService(): ServerServiceDefinition = HelloWorldServiceGrpc.bindService(this)
}
