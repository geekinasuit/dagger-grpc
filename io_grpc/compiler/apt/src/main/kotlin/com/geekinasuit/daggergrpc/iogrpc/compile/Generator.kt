package com.geekinasuit.daggergrpc.iogrpc.compile

import com.geekinasuit.kspbridge.apt.APTLogger
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment

abstract class Generator<I>(
  protected val env: RoundEnvironment,
  protected val logger: APTLogger,
  protected val filer: Filer,
) {
  abstract fun generate(input: I)
}
