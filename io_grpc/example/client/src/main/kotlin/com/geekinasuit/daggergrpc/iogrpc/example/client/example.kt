package com.geekinasuit.daggergrpc.iogrpc.example.client

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc
import com.linecorp.armeria.client.grpc.GrpcClients.newClient

const val URL = "http://127.0.0.1:8888/"

fun main(vararg args: String) {
  Client.hello()
  Client.goodbye()
  Client.whatever()
}

object Client {
  val helloWorldClient = newClient(URL, HelloWorldServiceGrpc.HelloWorldServiceBlockingStub::class.java)
  val whateverClient = newClient(URL, WhateverServiceGrpc.WhateverServiceBlockingStub::class.java)

  fun hello() {
    val request = HelloWorld.SayHelloRequest.newBuilder().setHelloText("Booyakashah").build()
    val response = helloWorldClient.sayHello(request)
    println("Server responded: ${response.responseText}")
  }

  fun goodbye() {
    val request = HelloWorld.SayGoodbyeRequest.newBuilder().setGoodbyeText("Booyakashah").build()
    val response = helloWorldClient.sayGoodbye(request)
    println("Server responded: ${response.responseText}")
  }

  fun whatever() {
    val request = WhateverRequest.newBuilder().setWhatever(true).build()
    val response = whateverClient.whatever(request)
    println("Server responded: ${response.suceeded}")
  }
}


