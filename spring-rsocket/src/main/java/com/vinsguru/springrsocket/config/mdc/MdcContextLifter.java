package com.vinsguru.springrsocket.config.mdc;

import com.vinsguru.springrsocket.config.MetadataContextHolder;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.Map;
import java.util.Set;

public class MdcContextLifter<T> implements CoreSubscriber<T> {
    private final CoreSubscriber<T> coreSubscriber;
    private final Set<String> keysToCopy;

    public MdcContextLifter(CoreSubscriber<T> coreSubscriber, Set<String> keysToCopy) {
        this.coreSubscriber = coreSubscriber;
        this.keysToCopy = keysToCopy;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        coreSubscriber.onSubscribe(subscription);
    }

    @Override
    public void onNext(T t) {
        copyToMdc(coreSubscriber.currentContext());
        coreSubscriber.onNext(t);
    }

    @Override
    public void onError(Throwable throwable) {
        coreSubscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
        coreSubscriber.onComplete();
    }

    @Override
    public Context currentContext() {
        return coreSubscriber.currentContext();
    }

    private void copyToMdc(Context context) {
        if (!context.isEmpty()) {
            Map<String, String> map = MetadataContextHolder.readFromContext(context, keysToCopy);
            if (CollectionUtils.isEmpty(map) == false) {
                MDC.setContextMap(map);
            }
        } else {
            MDC.clear();
        }
    }
}
