package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.linecorp.armeria.server.grpc.GrpcService
import io.grpc.BindableService
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import kotlin.reflect.KClass

/** Marks a class as a gRPC call handler. */
@Target(AnnotationTarget.CLASS) annotation class GrpcServiceHandler(val grpcWrapperType: KClass<*>)

fun wrapService(
  bindableService: BindableService,
  vararg interceptors: ServerInterceptor,
): GrpcService {
  return GrpcService.builder().addService(bindableService).intercept(*interceptors).build()
}

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
