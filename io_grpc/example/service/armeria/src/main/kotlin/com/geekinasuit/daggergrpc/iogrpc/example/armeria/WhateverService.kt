package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.geekinasuit.daggergrpc.api.GrpcServiceHandler
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverResponse
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc
import io.grpc.stub.StreamObserver

@GrpcServiceHandler(WhateverServiceGrpc::class)
class WhateverService : WhateverServiceGrpc.AsyncService {
  override fun whatever(
    request: WhateverRequest,
    responseObserver: StreamObserver<WhateverResponse>
  ) {
    val response = WhateverResponse.newBuilder().setSuceeded(request.whatever).build()
    responseObserver.onNext(response)
    responseObserver.onCompleted()
  }
}
