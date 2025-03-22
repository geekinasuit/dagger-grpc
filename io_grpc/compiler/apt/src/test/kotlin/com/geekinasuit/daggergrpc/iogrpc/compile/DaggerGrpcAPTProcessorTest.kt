package com.geekinasuit.daggergrpc.iogrpc.compile

import com.google.common.truth.Truth.assertAbout
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
  }
}

class Source(val fq: String) {
  fun withSource(src: String): JavaFileObject = JavaFileObjects.forSourceString(fq, src)
}
