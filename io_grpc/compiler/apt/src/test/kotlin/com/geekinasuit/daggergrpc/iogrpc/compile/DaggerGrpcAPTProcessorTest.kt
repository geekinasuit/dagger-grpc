package com.geekinasuit.daggergrpc.iogrpc.compile

import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.testing.compile.CompilationSubject.compilations
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import javax.tools.JavaFileObject
import org.junit.Test

class DaggerGrpcAPTProcessorTest {
  @Test
  fun testSimpleCompilation() {
    val src =
      Source("test.Source1")
        .withSource(
          """
          package test;
          import com.geekinasuit.daggergrpc.api.GrpcServiceHandler;
          @GrpcServiceHandler(grpcWrapperType = foo.FooServiceGrpc.class)
          class Source1 {}
          """
        )
    val compiler = javac().withProcessors(DaggerGrpcAPTProcessor())
    val result = compiler.compile(src)
    assertAbout(compilations()).that(result).succeeded()
    assertThat(result.generatedSourceFiles()).hasSize(1)

    val expected =
      Source("test.Source1Adapter")
        .withSource(
          """
              package test;
              
              import foo.Foo;
              import foo.FooServiceGrpc;
              import io.grpc.BindableService;
              import io.grpc.ServerServiceDefinition;
              import io.grpc.stub.StreamObserver;
              import java.lang.Override;
              import java.util.concurrent.Callable;
              import javax.annotation.processing.Generated;
              
              @Generated("com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcAPTProcessor")
              public class Source1Adapter implements BindableService, FooServiceGrpc.AsyncService {
                private final Callable<FooServiceGrpc.AsyncService> service;
              
                public Source1Adapter(Callable<FooServiceGrpc.AsyncService> service) {
                  this.service = service;
                }
              
                @Override
                public void foo(Foo.FooRequest request, StreamObserver responseObserver) {
                  try {
                        service.call().foo(request,responseObserver);
                      } catch (Exception e) {
                        throw new java.lang.RuntimeException(e);
                      };
                }
              
                @Override
                public ServerServiceDefinition bindService() {
                  return FooServiceGrpc.bindService(this);
                }
              }
            """
            .trimIndent()
        )

    assertAbout(compilations())
      .that(result)
      .generatedSourceFile("test.Source1Adapter")
      .hasSourceEquivalentTo(expected)
  }

  @Test
  fun testValidationFailAnnotationOnInterface() {
    val src =
      Source("test.Source1")
        .withSource(
          """
          package test;
          import com.geekinasuit.daggergrpc.api.GrpcServiceHandler;
          @GrpcServiceHandler(grpcWrapperType = foo.FooServiceGrpc.class)
          interface Source1 {}
          """
        )
    val compiler = javac().withProcessors(DaggerGrpcAPTProcessor())
    val result = compiler.compile(src)
    assertAbout(compilations()).that(result).failed()
    assertAbout(compilations()).that(result).hadErrorCount(1)
    assertAbout(compilations())
      .that(result)
      .hadErrorContaining("GrpcServiceHandler must only be used on a class")
  }
}

class Source(val fq: String) {
  fun withSource(src: String): JavaFileObject = JavaFileObjects.forSourceString(fq, src)
}
