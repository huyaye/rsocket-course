package com.vinsguru.springrsocket;

import com.vinsguru.springrsocket.dto.ComputationRequestDto;
import com.vinsguru.springrsocket.dto.ComputationResponseDto;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.time.Duration;

@SpringBootTest
@TestPropertySource(properties =
        {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.rsocket.RSocketServerAutoConfiguration"
        }
)
public class Lec08ConnectionRetryTest {
    @Autowired
    private RSocketRequester.Builder builder;

    /*
        Spring server 먼저 실행시키고, TC 수행.
        TC 수행중 Spring server 종료,재시작.
     */
    @Test
    public void connectionTest() throws InterruptedException {
        RSocketRequester requester1 = this.builder
                .rsocketConnector(c -> c.reconnect(Retry.fixedDelay(10, Duration.ofSeconds(2))
                                        .doBeforeRetry(s -> System.out.println("retrying " + s.totalRetriesInARow()))))
                .transport(TcpClientTransport.create("localhost", 6565));

        for (int i = 0; i < 50; i++) {
            Mono<ComputationResponseDto> mono = requester1.route("math.service.square")
                    .data(new ComputationRequestDto(i))
                    .retrieveMono(ComputationResponseDto.class)
                    .doOnNext(System.out::println);

            StepVerifier.create(mono)
                    .expectNextCount(1)
                    .verifyComplete();

            Thread.sleep(2000);
        }
    }
}
