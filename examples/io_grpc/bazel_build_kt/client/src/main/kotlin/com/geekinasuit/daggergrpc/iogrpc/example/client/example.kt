package com.geekinasuit.daggergrpc.iogrpc.example.client

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorldServiceGrpc.HelloWorldServiceBlockingStub
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc.WhateverServiceBlockingStub
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.sayGoodbyeRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.sayHelloRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.whateverRequest
import com.linecorp.armeria.client.grpc.GrpcClients.newClient

const val URL = "http://127.0.0.1:8888/"

fun main(vararg args: String) {
  Client.hello()
  Client.goodbye()
  Client.whatever()
}

object Client {
  // TODO: Use the coroutine stub with a more articulated client.
  val helloWorldClient = newClient(URL, HelloWorldServiceBlockingStub::class.java)
  val whateverClient = newClient(URL, WhateverServiceBlockingStub::class.java)

  fun hello() {
    val request = sayHelloRequest { helloText = "Booyakashah" }
    val response = helloWorldClient.sayHello(request)
    println("Server responded: ${response.responseText}")
  }

  fun goodbye() {
    val request = sayGoodbyeRequest { goodbyeText = "Jungle is massive!" }
    val response = helloWorldClient.sayGoodbye(request)
    println("Server responded: ${response.responseText}")
  }

  fun whatever() {
    val request = whateverRequest { whatever = true }
    val response = whateverClient.whatever(request)
    println("Server responded: ${response.suceeded}")
  }
}
