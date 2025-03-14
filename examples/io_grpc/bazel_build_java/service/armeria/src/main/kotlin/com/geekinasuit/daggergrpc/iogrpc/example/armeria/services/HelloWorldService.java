package com.geekinasuit.daggergrpc.iogrpc.example.armeria.services;

import static java.util.logging.Level.SEVERE;

import com.geekinasuit.daggergrpc.api.GrpcCallContext;
import com.geekinasuit.daggergrpc.api.GrpcCallScope;
import com.geekinasuit.daggergrpc.api.GrpcServiceHandler;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayGoodbyeRequest;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayGoodbyeResponse;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayHelloRequest;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayHelloResponse;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc.AsyncService;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;
import javax.inject.Inject;

@GrpcServiceHandler(grpcWrapperType = HelloWorldServiceGrpc.class)
@GrpcCallScope
public class HelloWorldService implements AsyncService {
  private static final Logger log = Logger.getLogger(HelloWorldService.class.toString());
  private final GrpcCallContext context;

  @Inject
  HelloWorldService(GrpcCallContext context) {
    this.context = context;
  }

  @Override
  public void sayHello(SayHelloRequest req, StreamObserver<SayHelloResponse> resp) {
    Metadata headers;
    try {
      headers = context.getHeaders();
    } catch (Exception e) {
      log.log(SEVERE, "Exception fetching grpc call context headers.", e);
      headers = new Metadata();
    }
    String responseText = "Hello: " + req.getHelloText() + "\nHeaders: $headers";
    SayHelloResponse response = SayHelloResponse.newBuilder().setResponseText(responseText).build();
    resp.onNext(response);
    resp.onCompleted();
  }

  @Override
  public void sayGoodbye(SayGoodbyeRequest req, StreamObserver<SayGoodbyeResponse> resp) {
    String responseText = "Goodbye: " + req.getGoodbyeText();
    SayGoodbyeResponse response =
        SayGoodbyeResponse.newBuilder().setResponseText(responseText).build();
    resp.onNext(response);
    resp.onCompleted();
  }
}
