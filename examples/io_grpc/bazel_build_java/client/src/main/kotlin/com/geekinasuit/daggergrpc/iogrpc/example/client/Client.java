package com.geekinasuit.daggergrpc.iogrpc.example.client;

import static com.linecorp.armeria.client.grpc.GrpcClients.newClient;

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayGoodbyeRequest;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayGoodbyeResponse;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayHelloRequest;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld.SayHelloResponse;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc.HelloWorldServiceBlockingStub;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverRequest;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverResponse;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc.WhateverServiceBlockingStub;

public class Client {
  private static final String URL = "http://127.0.0.1:8888/";
  private static final HelloWorldServiceBlockingStub helloWorldClient = newClient(URL, HelloWorldServiceBlockingStub.class);
  private static final WhateverServiceBlockingStub whateverClient = newClient(URL, WhateverServiceBlockingStub.class);

  public static void hello() {
    SayHelloRequest request = SayHelloRequest.newBuilder().setHelloText("Booyakashah").build();
    SayHelloResponse response = helloWorldClient.sayHello(request);
    System.out.println("Server responded: " + response.getResponseText());
  }

  public static void  goodbye() {
    SayGoodbyeRequest request = SayGoodbyeRequest.newBuilder().setGoodbyeText("Booyakashah").build();
    SayGoodbyeResponse response = helloWorldClient.sayGoodbye(request);
    System.out.println("Server responded: " + response.getResponseText());
  }

  public static void  whatever() {
    WhateverRequest request = WhateverRequest.newBuilder().setWhatever(true).build();
    WhateverResponse response = whateverClient.whatever(request);
    System.out.println("Server responded: " + response.getSuceeded());
  }

  public static void main(String... args) {
    Client.hello();
    Client.goodbye();
    Client.whatever();
  }
}


