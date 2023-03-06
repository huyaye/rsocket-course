package com.vinsguru.springrsocket.config.mdc;

import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;

import java.util.Set;

public class MdcContextLifterHook {
    private static final String MDC_CONTEXT_REACTOR_KEY = MdcContextLifterHook.class.getName();
    private final Set<String> keysToCopy;

    public MdcContextLifterHook(Set<String> keysToCopy) {
        this.keysToCopy = keysToCopy;
    }

    public void contextOperatorHook() {
        if (CollectionUtils.isEmpty(keysToCopy)) {
            return;
        }
        Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY,
                Operators.lift((scannable, coreSubscriber) -> new MdcContextLifter<>(coreSubscriber, keysToCopy)));
    }

    public void cleanupHook() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY);
    }
}
