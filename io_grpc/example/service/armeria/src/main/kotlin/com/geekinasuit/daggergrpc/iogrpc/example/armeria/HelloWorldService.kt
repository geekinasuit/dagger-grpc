package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc.AsyncService
import io.grpc.stub.StreamObserver

@GrpcServiceHandler(HelloWorldServiceGrpc::class)
class HelloWorldService : AsyncService {
  override fun sayHello(
    request: HelloWorld.SayHelloRequest,
    responseObserver: StreamObserver<HelloWorld.SayHelloResponse>
  ) {
    val responseText = "Hello: ${request.helloText}"
    val response = HelloWorld.SayHelloResponse.newBuilder().setResponseText(responseText).build()
    responseObserver.onNext(response)
    responseObserver.onCompleted()
  }

  override fun sayGoodbye(
    request: HelloWorld.SayGoodbyeRequest,
    responseObserver: StreamObserver<HelloWorld.SayGoodbyeResponse>
  ) {
    val responseText = "Goodbye: ${request.goodbyeText}"
    val response = HelloWorld.SayGoodbyeResponse.newBuilder().setResponseText(responseText).build()
    responseObserver.onNext(response)
    responseObserver.onCompleted()
  }
}
