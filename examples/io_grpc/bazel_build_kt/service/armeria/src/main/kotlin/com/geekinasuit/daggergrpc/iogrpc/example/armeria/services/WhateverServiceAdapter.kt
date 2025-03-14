package com.geekinasuit.daggergrpc.iogrpc.example.armeria.services

import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverRequest
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.Whatever.WhateverResponse
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc
import com.geekinasuit.daggergrpc.iogrpc.example.armeria.api.WhateverServiceGrpc.AsyncService
import io.grpc.BindableService
import io.grpc.ServerServiceDefinition
import io.grpc.stub.StreamObserver
import javax.annotation.Generated

@Generated("to be generated")
class WhateverServiceAdapter(val service: () -> AsyncService) : BindableService, AsyncService {
  override fun whatever(req: WhateverRequest, resp: StreamObserver<WhateverResponse>) =
    service().whatever(req, resp)

  override fun bindService(): ServerServiceDefinition = WhateverServiceGrpc.bindService(this)
}
