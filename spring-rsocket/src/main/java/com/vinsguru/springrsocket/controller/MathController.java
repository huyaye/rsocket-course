package com.vinsguru.springrsocket.controller;

import com.vinsguru.springrsocket.dto.ChartResponseDto;
import com.vinsguru.springrsocket.dto.ComputationRequestDto;
import com.vinsguru.springrsocket.dto.ComputationResponseDto;
import com.vinsguru.springrsocket.service.MathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

// Lec01RSocketTest
@Controller
public class MathController {

    @Autowired
    private MathService service;

    /**
     * java -jar rsc-0.9.1.jar --debug --fnf --data "{\"input\":5}" -m TID_123456 --mmt message/x.rsocket.trace.id.v0 --route math.service.print --stacktrace tcp://localhost:6565
     */
    @MessageMapping("math.service.print")
    public Mono<Void> print(Mono<ComputationRequestDto> requestDtoMono){
        return this.service.print(requestDtoMono);
    }

    /**
     * java -jar rsc-0.9.1.jar --debug --request --data "{\"input\":5}" -m TID_1234567 --mmt message/x.rsocket.trace.id.v0 --route math.service.square --stacktrace tcp://localhost:6565
     */
    @MessageMapping("math.service.square")
    public Mono<ComputationResponseDto> findSquare(Mono<ComputationRequestDto> requestDtoMono){
        return this.service.findSquare(requestDtoMono);
    }

    @MessageMapping("math.service.table")
    public Flux<ComputationResponseDto> tableStream(Mono<ComputationRequestDto> requestDtoMono){
        return requestDtoMono.flatMapMany(this.service::tableStream);
    }

    @MessageMapping("math.service.chart")
    public Flux<ChartResponseDto> chartStream(Flux<ComputationRequestDto> requestDtoFlux){
        return this.service.chartStream(requestDtoFlux);
    }

    @MessageExceptionHandler
    Mono<Map<String, String>> handleException(Exception exception) {
        return Mono.just(Collections.singletonMap("error", exception.getMessage()));
    }

}
