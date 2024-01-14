package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.HelloWorld
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverResponse
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc
import io.grpc.stub.StreamObserver

class WhateverService : WhateverServiceGrpc.AsyncService {
  override fun whatever(
    request: Whatever.WhateverRequest,
    responseObserver: StreamObserver<Whatever.WhateverResponse>
  ) {
    val response = WhateverResponse.newBuilder().setSuceeded(request.whatever).build()
    responseObserver.onNext(response)
    responseObserver.onCompleted()
  }
}
