package com.geekinasuit.daggergrpc.iogrpc.example.armeria;

import static com.geekinasuit.daggergrpc.armeria.Armeria_grpc_utilKt.wrapService;

import com.geekinasuit.daggergrpc.api.GrpcCallContext;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger.ApplicationGraph;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import io.grpc.BindableService;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import javax.inject.Inject;

public class ExampleServer {
  private static final Logger log = Logger.getLogger(ExampleServer.class.toString());
  private static final int PORT = 8888;
  private final Set<BindableService> services;

  @Inject
  ExampleServer(Set<BindableService> services) {
    this.services = services;
  }

  public Server setup() {
    log.info("startup");
    ServerBuilder builder = Server.builder().http(PORT);
    for (BindableService service : services) {
      builder.service(wrapService(service, new GrpcCallContext.Interceptor()));
    }
    return builder.build();
  }

  public static void main(String... args) {
    // Build the outer object application-scoped object graph.
    ApplicationGraph graph = ApplicationGraph.builder().build();

    // Get the dagger-injected server and execute setup.
    Server server = graph.server().setup();

    // Register a shutdown hook to kill the server properly if the JVM is going to shut down.
    CompletableFuture<Void> future = server.closeOnJvmShutdown();
    future.thenRun(() -> log.info("Server has been stopped."));

    // start the server and block the main method on the server's main thread.
    server.start().join();
  }
}
