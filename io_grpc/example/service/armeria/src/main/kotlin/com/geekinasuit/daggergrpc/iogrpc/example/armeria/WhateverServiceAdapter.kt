package com.geekinasuit.daggergrpc.iogrpc.example.armeria

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc.AsyncService
import io.grpc.BindableService
import io.grpc.ServerServiceDefinition
import io.grpc.stub.StreamObserver

class WhateverServiceAdapter(val service: () -> AsyncService) : BindableService, AsyncService {
  override fun whatever(
    request: Whatever.WhateverRequest,
    responseObserver: StreamObserver<Whatever.WhateverResponse>
  ) = service().whatever(request, responseObserver)

  override fun bindService(): ServerServiceDefinition = WhateverServiceGrpc.bindService(this)
}
