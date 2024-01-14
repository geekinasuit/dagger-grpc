package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

fun main(vararg args: String) {

  val server = ExampleServer().build()

  server.closeOnJvmShutdown().thenRun { log.info { "Server has been stopped." } }
  server.start().join()
}
