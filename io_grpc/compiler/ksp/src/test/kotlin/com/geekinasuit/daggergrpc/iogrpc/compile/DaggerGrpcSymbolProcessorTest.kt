package com.geekinasuit.daggergrpc.iogrpc.compile

import com.tschuchort.compiletesting.SourceFile
import org.junit.Test

class DaggerGrpcSymbolProcessorTest {
  private val handlerSource =
    SourceFile.kotlin(
      "FooService.kt",
      """
      package foo

      import com.geekinasuit.daggergrpc.api.GrpcCallScope
      import com.geekinasuit.daggergrpc.api.GrpcServiceHandler
      import foo.Foo.FooRequest
      import foo.Foo.FooResponse
      import foo.FooServiceGrpc
      import io.grpc.stub.StreamObserver
      import javax.inject.Inject
      
      @GrpcServiceHandler(FooServiceGrpc::class)
      @GrpcCallScope
      class FooService @Inject constructor() : FooServiceGrpc.AsyncService {
        override fun foo(request: FooRequest,  responseObserver: StreamObserver<FooResponse>,
      ) {
          val response = FooResponse.newBuilder().setSuceeded(request.foo).build()
          responseObserver.onNext(response)
          responseObserver.onCompleted()
        }
      }
    """
        .trimIndent(),
    )

  @Test
  fun testHandlerGenerator() {
    // TODO(cgruber): Fix this, once kotlin-compile-testing works with Kotlin2.
    //       or I figure out how to suppress the experimental API stuff.

    //    val compilation =
    //      KotlinCompilation().apply {
    //        sources = listOf(handlerSource)
    //        symbolProcessorProviders = listOf(DaggerGrpcProcessorProvider())
    //      }
    //    val result = compilation.compile()
  }
}
