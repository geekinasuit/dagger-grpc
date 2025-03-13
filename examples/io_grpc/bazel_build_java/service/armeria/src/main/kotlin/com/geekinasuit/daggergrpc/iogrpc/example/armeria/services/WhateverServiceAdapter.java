package com.geekinasuit.daggergrpc.iogrpc.example.armeria.services;

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc.AsyncService;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("to be generated")
public class WhateverServiceAdapter implements BindableService, AsyncService {
  private final Callable<AsyncService> service;

  public WhateverServiceAdapter(Callable<AsyncService> service) {
    this.service = service;
  }

  @Override
  public void whatever(
      Whatever.WhateverRequest req, StreamObserver<Whatever.WhateverResponse> resp) {
    try {
      service.call().whatever(req, resp);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ServerServiceDefinition bindService() {
    return WhateverServiceGrpc.bindService(this);
  }
}
