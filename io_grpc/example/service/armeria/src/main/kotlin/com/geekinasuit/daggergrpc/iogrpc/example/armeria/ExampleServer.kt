package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.geekinasuit.daggergrpc.armeria.wrapService
import com.linecorp.armeria.server.Server
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.BindableService
import javax.inject.Inject

private val log = KotlinLogging.logger {}

const val PORT = 8888

class ExampleServer
@Inject
constructor(private val services: Set<@JvmSuppressWildcards BindableService>) {

  fun setup(): Server {
    log.info { "startup" }
    return Server.builder()
      .http(PORT)
      .apply { services.forEach { this.service(wrapService(it)) } }
      .build()
  }
}
