package com.vinsguru.rsocket.service;

import com.vinsguru.rsocket.dto.ChartResponseDto;
import com.vinsguru.rsocket.dto.RequestDto;
import com.vinsguru.rsocket.dto.ResponseDto;
import com.vinsguru.rsocket.util.ObjectUtil;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class MathService implements RSocket {

    // java -jar rsc-0.9.1.jar --debug --fnf --data "{\"input\":5}" --stacktrace tcp://localhost:6565
    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        System.out.println("[fireAndForget] Receiving : " + ObjectUtil.toObject(payload, RequestDto.class));
        return Mono.empty();
    }

    // java -jar rsc-0.9.1.jar --debug --request --data "{\"input\":5}" --stacktrace tcp://localhost:6565
    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        return Mono.fromSupplier(() -> {
            RequestDto requestDto = ObjectUtil.toObject(payload, RequestDto.class);
            System.out.println("[RequestResponse] Receiving : " + ObjectUtil.toObject(payload, RequestDto.class));
            ResponseDto responseDto = new ResponseDto(requestDto.getInput(), requestDto.getInput() * requestDto.getInput());
            return ObjectUtil.toPayload(responseDto);
        });
    }

    // java -jar rsc-0.9.1.jar --debug --stream --take 4 --data "{\"input\":5}" --stacktrace tcp://localhost:6565
    @Override
    public Flux<Payload> requestStream(Payload payload) {
        RequestDto requestDto = ObjectUtil.toObject(payload, RequestDto.class);
        return Flux.range(1, 10)
                    .map(i -> i * requestDto.getInput())
                    .map(i -> new ResponseDto(requestDto.getInput(), i))
                    .delayElements(Duration.ofSeconds(1))
                    .doOnNext(s -> System.out.println("[onNext] " + s))
                    .doFinally(s -> System.out.println(s))
                    .map(ObjectUtil::toPayload);
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        return Flux.from(payloads)
                    .map(p -> ObjectUtil.toObject(p, RequestDto.class))
                    .map(RequestDto::getInput)
                    .map(i -> new ChartResponseDto(i, (i * i) + 1))
                    .map(ObjectUtil::toPayload);
    }
}
