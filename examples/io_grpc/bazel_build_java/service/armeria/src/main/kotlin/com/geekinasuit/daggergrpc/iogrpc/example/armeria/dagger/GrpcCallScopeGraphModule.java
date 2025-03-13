package com.geekinasuit.daggergrpc.iogrpc.example.armeria.dagger;

import com.geekinasuit.daggergrpc.api.GrpcCallContext;
import dagger.Module;

@Module(includes = {GrpcCallContext.Module.class})
interface GrpcCallScopeGraphModule {}
