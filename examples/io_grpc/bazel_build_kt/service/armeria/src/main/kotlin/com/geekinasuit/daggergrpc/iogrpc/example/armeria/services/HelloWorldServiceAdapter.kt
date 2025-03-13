package com.geekinasuit.daggergrpc.iogrpc.example.armeria.services

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayGoodbyeRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayGoodbyeResponse
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayHelloRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayHelloResponse
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc.AsyncService
import io.grpc.BindableService
import io.grpc.ServerServiceDefinition
import io.grpc.stub.StreamObserver
import javax.annotation.Generated

@Generated("to be generated")
class HelloWorldServiceAdapter(val service: () -> AsyncService) : BindableService, AsyncService {
  override fun sayHello(req: SayHelloRequest, resp: StreamObserver<SayHelloResponse>) =
    service().sayHello(req, resp)

  override fun sayGoodbye(req: SayGoodbyeRequest, resp: StreamObserver<SayGoodbyeResponse>) =
    service().sayGoodbye(req, resp)

  override fun bindService(): ServerServiceDefinition = HelloWorldServiceGrpc.bindService(this)
}
