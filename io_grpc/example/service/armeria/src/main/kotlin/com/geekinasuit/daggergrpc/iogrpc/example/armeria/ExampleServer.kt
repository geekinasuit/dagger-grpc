package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.linecorp.armeria.server.Server
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

const val PORT = 8888

class ExampleServer {

  fun build(): Server {
    log.info { "startup" }
    val services =
      listOf(
        wrapService(HelloWorldServiceAdapter { HelloWorldService() }, GrpcCallContextInterceptor),
        wrapService(WhateverServiceAdapter { WhateverService() }, GrpcCallContextInterceptor)
      )
    return Server.builder().http(PORT).apply { services.forEach { this.service(it) } }.build()
  }
}
