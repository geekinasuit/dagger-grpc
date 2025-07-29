package com.geekinasuit.daggergrpc.iogrpc.compile

import com.google.testing.compile.JavaFileObjects
import javax.tools.JavaFileObject

class Source(val fq: String) {
  fun withSource(src: String): JavaFileObject = JavaFileObjects.forSourceString(fq, src)
}
