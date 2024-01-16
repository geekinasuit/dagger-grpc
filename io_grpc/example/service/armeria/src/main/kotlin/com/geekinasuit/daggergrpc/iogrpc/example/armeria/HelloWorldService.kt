package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.geekinasuit.daggergrpc.api.GrpcCallScope
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc.AsyncService
import io.grpc.stub.StreamObserver
import com.geekinasuit.daggergrpc.api.GrpcServiceHandler
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayGoodbyeRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayGoodbyeResponse
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayHelloRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayHelloResponse
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger.Blah
import javax.inject.Inject

@GrpcServiceHandler(HelloWorldServiceGrpc::class)
@GrpcCallScope
class HelloWorldService @Inject constructor(private val blah: Blah): AsyncService {
  override fun sayHello(
          request: SayHelloRequest,
          responseObserver: StreamObserver<SayHelloResponse>
  ) {
    val responseText = "Hello: ${request.helloText} ${blah.blah}"
    val response = SayHelloResponse.newBuilder().setResponseText(responseText).build()
    responseObserver.onNext(response)
    responseObserver.onCompleted()
  }

  override fun sayGoodbye(
      request: SayGoodbyeRequest,
      responseObserver: StreamObserver<SayGoodbyeResponse>
  ) {
    val responseText = "Goodbye: ${request.goodbyeText}"
    val response = SayGoodbyeResponse.newBuilder().setResponseText(responseText).build()
    responseObserver.onNext(response)
    responseObserver.onCompleted()
  }
}
