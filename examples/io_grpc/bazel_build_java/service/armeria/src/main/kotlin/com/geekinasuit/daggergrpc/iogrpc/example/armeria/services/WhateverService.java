package com.geekinasuit.daggergrpc.iogrpc.example.armeria.services;

import com.geekinasuit.daggergrpc.api.GrpcCallScope;
import com.geekinasuit.daggergrpc.api.GrpcServiceHandler;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverRequest;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverResponse;
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc;
import io.grpc.stub.StreamObserver;
import javax.inject.Inject;

@GrpcServiceHandler(grpcWrapperType = WhateverServiceGrpc.class)
@GrpcCallScope
public class WhateverService implements WhateverServiceGrpc.AsyncService {
  @Inject
  WhateverService() {}

  @Override
  public void whatever(WhateverRequest req, StreamObserver<WhateverResponse> resp) {
    WhateverResponse response =
        WhateverResponse.newBuilder().setSuceeded(req.getWhatever()).build();
    resp.onNext(response);
    resp.onCompleted();
  }
}
