package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.geekinasuit.daggergrpc.api.GrpcCallScope
import com.geekinasuit.daggergrpc.api.GrpcServiceHandler
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverResponse
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc
import io.grpc.stub.StreamObserver
import javax.inject.Inject

@GrpcServiceHandler(WhateverServiceGrpc::class)
@GrpcCallScope
class WhateverService @Inject constructor(): WhateverServiceGrpc.AsyncService {
  override fun whatever(
    request: WhateverRequest,
    responseObserver: StreamObserver<WhateverResponse>
  ) {
    val response = WhateverResponse.newBuilder().setSuceeded(request.whatever).build()
    responseObserver.onNext(response)
    responseObserver.onCompleted()
  }
}
