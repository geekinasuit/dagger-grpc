package com.geekinasuit.daggergrpc.iogrpc.compile

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.CompilationSubject.compilations
import com.google.testing.compile.Compiler.javac
import dagger.internal.codegen.ComponentProcessor
import org.junit.Test

class ModuleGeneratorTest {
  private val adapter =
    Source("test.FooService")
      .withSource(
        """ 
        package test;
        
        import com.geekinasuit.daggergrpc.api.GrpcCallScope;
        import com.geekinasuit.daggergrpc.api.GrpcServiceHandler;
        
        import io.grpc.stub.StreamObserver;
        import javax.inject.Inject;
        
        @GrpcServiceHandler(grpcWrapperType = foo.FooServiceGrpc.class)
        @GrpcCallScope
        public class FooService implements foo.FooServiceGrpc.AsyncService {
          @Inject
          FooService() {}
        
          @Override
          public void foo(foo.Foo.FooRequest req, StreamObserver<foo.Foo.FooResponse> resp) {
            foo.Foo.FooResponse response =
                foo.Foo.FooResponse.newBuilder() 
                    .setSucceeded(true)
                    .build();
            resp.onNext(response);
            resp.onCompleted();
          }
        }
        """
          .trimIndent()
      )
  private val server =
    Source("test.MainServer")
      .withSource(
        """ 
        package test;
        
        import com.geekinasuit.daggergrpc.api.GrpcCallContext;
        import test.ApplicationGraph;
        import com.linecorp.armeria.server.Server;
        import com.linecorp.armeria.server.ServerBuilder;
        import com.linecorp.armeria.server.grpc.GrpcService;
        import io.grpc.BindableService;
        import java.util.Set;
        import java.util.concurrent.CompletableFuture;
        import java.util.logging.Logger;
        import javax.inject.Inject;
        
        public class MainServer {
          private static final Logger log = Logger.getLogger(MainServer.class.toString());
          private static final int PORT = 8888;
          private final Set<BindableService> services;
        
          @Inject
          MainServer(Set<BindableService> services) {
            this.services = services;
          }
        
          public Server setup() {
            log.info("startup");
            ServerBuilder builder = Server.builder().http(PORT);
            for (BindableService service : services) {
              builder.service(
                  GrpcService.builder()
                      .addService(service)
                      .intercept(new GrpcCallContext.Interceptor())
                      .build());
            }
            return builder.build();
          }
        
          public static void main(String... args) {
            ApplicationGraph graph = ApplicationGraph.builder().build();
            Server server = graph.server().setup();
            CompletableFuture<Void> future = server.closeOnJvmShutdown();
            future.thenRun(() -> log.info("Server has been stopped."));
            server.start().join();
          }
        }
        """
          .trimIndent()
      )
  private val appGraph =
    Source("test.ApplicationGraph")
      .withSource(
        """
        package test;
        
        import com.geekinasuit.daggergrpc.api.ApplicationScope;
        import com.geekinasuit.daggergrpc.api.GrpcApplication;
        import com.geekinasuit.daggergrpc.api.GrpcCallScope;
        import dagger.Component;
        import dagger.Provides;
        import java.util.concurrent.atomic.AtomicInteger;
        import javax.inject.Named;
        
        @GrpcApplication(callScopedModules = {String.class})
        @Component(modules = {Grpc.Module.class})
        @ApplicationScope
        public interface ApplicationGraph extends Grpc.CallScopeGraph.Supplier {
          MainServer server();
        
          @Component.Builder
          interface Builder {
            ApplicationGraph build();
          }
          
          static Builder builder() {
            return DaggerApplicationGraph.builder();
          }
        }
        """
      )

  @Test
  fun testFoo() {
    val expected =
      Source("test.Grpc")
        .withSource(
          """
          package test;
          
          import com.geekinasuit.daggergrpc.api.GrpcCallContext;
          import com.geekinasuit.daggergrpc.api.GrpcCallScope;
          import dagger.Binds;
          import dagger.Provides;
          import dagger.Subcomponent;
          import dagger.multibindings.IntoSet;
          import io.grpc.BindableService;
          import javax.annotation.processing.Generated;
          
          @Generated("com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcAPTProcessor")
          public interface Grpc {
            @Subcomponent(
                modules = { GrpcCallContext.Module.class }
            )
            @GrpcCallScope
            interface CallScopeGraph {
              FooService fooService();
          
              interface Supplier {
                CallScopeGraph callScope();
              }
            }
          
            @dagger.Module
            abstract class Module {
              @Binds
              public abstract CallScopeGraph.Supplier callScopeGraphSupplier(ApplicationGraph impl);
          
              @Provides
              @IntoSet
              public static BindableService provideFooService(CallScopeGraph.Supplier supplier) {
                return new FooServiceAdapter(() -> supplier.callScope().fooService());
              }
            }
          }
          """
            .trimIndent()
        )

    val compiler = javac().withProcessors(DaggerGrpcAPTProcessor(), ComponentProcessor())
    val result = compiler.compile(adapter, server, appGraph)
    assertAbout(compilations())
      .that(result)
      .generatedSourceFile("test.Grpc")
      .hasSourceEquivalentTo(expected)
  }
}
