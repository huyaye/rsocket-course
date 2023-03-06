package com.vinsguru.springrsocket.config.log;

import io.rsocket.RSocket;
import io.rsocket.plugins.RSocketInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.MetadataExtractor;

@Slf4j
@RequiredArgsConstructor
public class LogRSocketInterceptor implements RSocketInterceptor {
    private final MetadataExtractor metadataExtractor;

    @Override
    public RSocket apply(RSocket rSocket) {
        return new LogRSocketProxy(rSocket, metadataExtractor);
    }
}
