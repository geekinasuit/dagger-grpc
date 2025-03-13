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
    try (Server server = ApplicationGraph.builder().build().server().setup()) {
      CompletableFuture<Void> future = server.closeOnJvmShutdown();
      future.thenRun(() -> log.info("Server has been stopped."));
      server.start().join();
    }
  }
}
