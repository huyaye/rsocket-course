package com.vinsguru.springrsocket.config;

import com.vinsguru.springrsocket.config.log.LogRSocketInterceptor;
import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketStrategies;

import java.time.Duration;

@Configuration
public class RSocketServerConfig {
    @Bean
    public RSocketServerCustomizer customizer(){
        return c -> c.resume(resumeStrategy());
    }

    @Bean
    public RSocketStrategiesCustomizer addMetadataExtractMimeTypeCustomizer() {
        return (strategyBuilder) ->
                strategyBuilder.metadataExtractorRegistry(register ->
                        register.metadataToExtract(TraceConst.TRACE_ID_MIME_TYPE, String.class, TraceConst.TRACE_ID));
    }

    @Bean
    RSocketServerCustomizer rSocketServerCustomizer(RSocketStrategies rSocketStrategies) {
        return (rSocketServer) ->
                rSocketServer.payloadDecoder(PayloadDecoder.ZERO_COPY)
                        .interceptors(interceptorRegistry ->
                                interceptorRegistry.forResponder(new LogRSocketInterceptor(rSocketStrategies.metadataExtractor())));
    }

    private Resume resumeStrategy(){
        return new Resume()
                        .sessionDuration(Duration.ofMinutes(5));
    }

}
