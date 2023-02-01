package com.vinsguru.rsocket.service;

import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import reactor.core.publisher.Mono;

public class SocketAcceptorImpl implements SocketAcceptor {
    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload connectionSetupPayload, RSocket rSocket) {
        System.out.println("SocketAcceptorImpl-accept method");
        // 테스트방법 : 하나의 항목만 주석해제 후 서버 실행. 클라이언트는 test case로 실행

        /**
         * 1. simple test (Lec01RSocketTest)
         */
        //  return Mono.fromCallable(MathService::new);

        /**
         * 2. p2p test (Lec02CallbackTest)
         */
        //  return Mono.fromCallable(() -> new BatchJobService(rSocket));

        /**
         * 3. backpressure test (Lec03BackpressureTest)
         */
        // return Mono.fromCallable(FastProducerService::new);

        /**
         * 4. connection setup test (Lec05ConnectionSetupTest)
         */
        if(isValidClient(connectionSetupPayload.getDataUtf8()))
            return Mono.just(new MathService());
        else
            return Mono.just(new FreeService());


    }

    private boolean isValidClient(String credentials){
        return "user:password".equals(credentials);
    }

}
