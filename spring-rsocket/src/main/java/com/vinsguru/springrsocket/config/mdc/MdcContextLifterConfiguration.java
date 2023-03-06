package com.vinsguru.springrsocket.config.mdc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

import static com.vinsguru.springrsocket.config.TraceConst.TRACE_ID;

@Configuration
public class MdcContextLifterConfiguration {
    @Bean(initMethod = "contextOperatorHook", destroyMethod = "cleanupHook")
    public MdcContextLifterHook mdcContextLifterHook() {
        return new MdcContextLifterHook(Collections.singleton(TRACE_ID));
    }
}
