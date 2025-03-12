package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.geekinasuit.daggergrpc.api.GrpcCallContext
import com.geekinasuit.daggergrpc.api.GrpcCallScope
import com.geekinasuit.daggergrpc.api.GrpcServiceHandler
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayGoodbyeRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayGoodbyeResponse
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayHelloRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayHelloResponse
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc.AsyncService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.Metadata
import io.grpc.stub.StreamObserver
import javax.inject.Inject

@GrpcServiceHandler(HelloWorldServiceGrpc::class)
@GrpcCallScope
class HelloWorldService @Inject constructor(private val context: GrpcCallContext) : AsyncService {
  override fun sayHello(
    request: SayHelloRequest,
    responseObserver: StreamObserver<SayHelloResponse>
  ) {
    val headers: Metadata =
      try {
        context.headers
      } catch (e: Exception) {
        log.error(e) { "Exception fetching grpc call context headers." }
        Metadata()
      }
    val responseText = "Hello: ${request.helloText}\nHeaders: $headers"
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

  companion object {
    private val log = KotlinLogging.logger {}
  }
}
