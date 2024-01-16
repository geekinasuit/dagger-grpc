package com.geekinasuit.daggergrpc.api

import kotlin.reflect.KClass

/** Marks a class as a gRPC call handler. */
@Target(AnnotationTarget.CLASS) annotation class GrpcServiceHandler(val grpcWrapperType: KClass<*>)
