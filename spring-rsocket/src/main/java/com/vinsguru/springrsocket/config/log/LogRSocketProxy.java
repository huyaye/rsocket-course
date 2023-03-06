package com.vinsguru.springrsocket.config.log;

import com.vinsguru.springrsocket.config.MetadataContextHolder;
import io.netty.util.ReferenceCountUtil;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.util.RSocketProxy;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
public class LogRSocketProxy extends RSocketProxy {
    private static final MimeType MESSAGE_RSOCKET_COMPOSITE_METADATA = MimeTypeUtils.parseMimeType(
            WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString());
    private final MetadataExtractor metadataExtractor;

    public LogRSocketProxy(RSocket source, MetadataExtractor metadataExtractor) {
        super(source);
        this.metadataExtractor = metadataExtractor;
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        ReferenceCountUtil.retain(payload);
        return Mono.just(payload)
                .doOnNext(p -> logRequest("Fire", p))
                .flatMap(super::fireAndForget)
                .doFinally(signalType -> {
                    ReferenceCountUtil.release(payload);
                })
                .subscriberContext(ctx ->
                        MetadataContextHolder.setContext(metadataExtractor.extract(payload, MESSAGE_RSOCKET_COMPOSITE_METADATA)));
    }


    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        ReferenceCountUtil.retain(payload);
        return Mono.just(payload)
                .doOnNext(p -> logRequest("Request", p))
                .flatMap(super::requestResponse)
                .doOnNext(p -> logResponse(p))
                .doFinally(signalType -> {
                    ReferenceCountUtil.release(payload);
                })
                .subscriberContext(ctx ->
                        MetadataContextHolder.setContext(metadataExtractor.extract(payload, MESSAGE_RSOCKET_COMPOSITE_METADATA)));
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        log.info("requestStream()");
        return super.requestStream(payload);
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        log.info("requestChannel()");
        return super.requestChannel(payloads);
    }

    private void logRequest(String type, Payload payload) {
        Map<String, Object> metadata = metadataExtractor.extract(payload, MESSAGE_RSOCKET_COMPOSITE_METADATA);
        log.info("[{}] route : {}, data : {}", type, metadata.get("route"), payload.getDataUtf8());
    }

    private void logResponse(Payload payload) {
        log.info("[Response] data : {}", payload.getDataUtf8());
    }
}
