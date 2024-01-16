package com.geekinasuit.daggergrpc.iogrpc.example.armeria


import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor



/**
 * An interceptor, which puts the [GrpcCallContext] (containing http headers [Metadata] and
 * [ServerCall] into a [ThreadLocal] for consumption in a call-scoped object graph.
 */
object GrpcCallContextInterceptor : ServerInterceptor {
  val callContextThreadLocal = ThreadLocal<GrpcCallContext>()

  override fun <A, B> interceptCall(
    call: ServerCall<A, B>,
    headers: Metadata,
    next: ServerCallHandler<A, B>
  ): ServerCall.Listener<A> {
    callContextThreadLocal.set(GrpcCallContext(headers, call))
    return next.startCall(call, headers)
  }
}

data class GrpcCallContext(val headers: Metadata, val call: ServerCall<*, *>)
