package com.vinsguru.springrsocket;

import com.vinsguru.springrsocket.config.TraceConst;
import com.vinsguru.springrsocket.dto.ChartResponseDto;
import com.vinsguru.springrsocket.dto.ComputationRequestDto;
import com.vinsguru.springrsocket.dto.ComputationResponseDto;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Lec01RSocketTest {

	private RSocketRequester requester;

	@Autowired
	private RSocketRequester.Builder builder;

	private ExecutorService executorService = Executors.newFixedThreadPool(3);

	@BeforeAll
	public void setup(){
		this.requester = this.builder
				.transport(TcpClientTransport.create("localhost", 6565));
	}

	@Test
	public void fireAndForget(){
		Mono<Void> mono = this.requester.route("math.service.print")
				.metadata("TID_00001", TraceConst.TRACE_ID_MIME_TYPE)
				.data(new ComputationRequestDto(5))
				.send();

		StepVerifier.create(mono)
				.verifyComplete();
	}

	@Test
	public void requestResponse(){
		Mono<ComputationResponseDto> mono = this.requester.route("math.service.square")
				.metadata("TID_00002", TraceConst.TRACE_ID_MIME_TYPE)
				.data(new ComputationRequestDto(5))
				.retrieveMono(ComputationResponseDto.class)
				.doOnNext(System.out::println);

		StepVerifier.create(mono)
				.expectNextCount(1)
				.verifyComplete();
	}

	@Test
	public void requestStream() throws Exception {
		for (int i = 0; i < 3; i++) {
			executorService.execute(() -> {
				Flux<ComputationResponseDto> flux = requester.route("math.service.table")
						.data(new ComputationRequestDto(5))
						.retrieveFlux(ComputationResponseDto.class)
						.doOnNext(System.out::println);

				StepVerifier.create(flux)
						.expectNextCount(10)
						.verifyComplete();
			});
			Thread.sleep(2000);
		}
		Thread.sleep(1000000);
	}

	@Test
	public void requestChannel(){

		Flux<ComputationRequestDto> dtoFlux = Flux.range(-10, 21)
				.map(ComputationRequestDto::new);

		Flux<ChartResponseDto> flux = this.requester.route("math.service.chart")
				.data(dtoFlux)
				.retrieveFlux(ChartResponseDto.class)
				.doOnNext(System.out::println);

		StepVerifier.create(flux)
				.expectNextCount(21)
				.verifyComplete();
	}


}
