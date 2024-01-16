package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger.ApplicationGraph
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

fun main(vararg args: String) {

  val server = ApplicationGraph.builder().build().server().setup()

  server.closeOnJvmShutdown().thenRun { log.info { "Server has been stopped." } }
  server.start().join()
}
