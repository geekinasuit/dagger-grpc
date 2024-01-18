package com.geekinasuit.daggergrpc.api

import javax.inject.Scope
import kotlin.reflect.KClass

/**
 * Marks the annotated object as being memoized for the life of the application
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Scope
annotation class ApplicationScope
