package com.vinsguru.springrsocket;

import com.vinsguru.springrsocket.dto.ComputationRequestDto;
import com.vinsguru.springrsocket.dto.ComputationResponseDto;
import io.rsocket.core.Resume;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.time.Duration;

@SpringBootTest
@TestPropertySource(properties =
        {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.rsocket.RSocketServerAutoConfiguration"
        }
)
public class Lec09SessionResumptionTest {

    @Autowired
    private RSocketRequester.Builder builder;

    /**
     * nginx, spring server 먼저 실행 후 테스트
     * TC 수행중 nginx 종료,재시작
     * 세션테스트를 위해서 proxy를 종료,재시작하는 식으로 테스트
     */
    @Test
    public void connectionTest() {
        RSocketRequester requester = this.builder
                .rsocketConnector(c -> c
                        .resume(resumeStrategy())
                        .reconnect(retryStrategy()))    // Stream 응답일 경우, Client에서는 connect시도를 하지 않는다.
                .transport(TcpClientTransport.create("localhost", 6566));

        Flux<ComputationResponseDto> flux = requester.route("math.service.table")
                .data(new ComputationRequestDto(5))
                .retrieveFlux(ComputationResponseDto.class)
                .doOnNext(System.out::println);

        StepVerifier.create(flux)
                .expectNextCount(1000)
                .verifyComplete();
    }

    private Resume resumeStrategy(){
        return new Resume()
                    .retry(Retry.fixedDelay(2000, Duration.ofSeconds(2))
                            .doBeforeRetry(s -> System.out.println("resume - retry :" + s.totalRetriesInARow())));
    }

    private Retry retryStrategy(){
        return Retry.fixedDelay(10 , Duration.ofSeconds(1))
                    .doBeforeRetry(s -> System.out.println("Retrying connection : " + s.totalRetriesInARow()));
    }

}
