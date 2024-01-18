package com.geekinasuit.daggergrpc.api

import dagger.Provides
import io.grpc.Metadata
import io.grpc.SecurityLevel
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import javax.inject.Inject

/**
 * The context for this GRPC call, which contains the call itself, as well as the HTTP or other
 * transport headers used for this call.
 */
@GrpcCallScope
class GrpcCallContext
@Inject
constructor(private val ctx: dagger.Lazy<Pair<Metadata, ServerCall<*, *>>>) {
  val headers: Metadata
    get() = ctx.get().first

  val authority: String?
    get() = ctx.get().second.authority

  val attributes: io.grpc.Attributes
    get() = ctx.get().second.attributes

  val securityLevel: SecurityLevel
    get() = ctx.get().second.securityLevel

  val isReady: Boolean
    get() = ctx.get().second.isReady

  val isCancelled: Boolean
    get() = ctx.get().second.isCancelled

  @dagger.Module
  object Module {
    @Provides
    @GrpcCallScope
    fun context(): Pair<Metadata, ServerCall<*, *>> = Interceptor.callContextThreadLocal.get()
  }

  /**
   * A [ServerInterceptor], which puts the [GrpcCallContext] (containing http headers [Metadata] and
   * [ServerCall] into a [ThreadLocal] for consumption in a call-scoped object graph.
   */
  class Interceptor : ServerInterceptor {
    override fun <A, B> interceptCall(
      call: ServerCall<A, B>,
      headers: Metadata,
      next: ServerCallHandler<A, B>
    ): ServerCall.Listener<A> {
      callContextThreadLocal.set(Pair(headers, call))
      return next.startCall(call, headers)
    }

    companion object {
      val callContextThreadLocal = ThreadLocal<Pair<Metadata, ServerCall<*, *>>>()
    }
  }
}
