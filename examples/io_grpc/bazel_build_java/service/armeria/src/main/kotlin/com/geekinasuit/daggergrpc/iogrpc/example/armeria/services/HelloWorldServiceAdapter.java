package com.geekinasuit.daggergrpc.iogrpc.example.armeria.services;

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc.AsyncService;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("to be generated")
public class HelloWorldServiceAdapter implements BindableService, AsyncService {
  private final Callable<AsyncService> service;

  public HelloWorldServiceAdapter(Callable<AsyncService> service) {
    this.service = service;
  }

  @Override
  public void sayHello(
      HelloWorld.SayHelloRequest req, StreamObserver<HelloWorld.SayHelloResponse> resp) {
    try {
      service.call().sayHello(req, resp);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sayGoodbye(
      HelloWorld.SayGoodbyeRequest req, StreamObserver<HelloWorld.SayGoodbyeResponse> resp) {
    try {
      service.call().sayGoodbye(req, resp);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ServerServiceDefinition bindService() {
    return HelloWorldServiceGrpc.bindService(this);
  }
}
