package com.vinsguru.springrsocket.config;

import org.springframework.util.MimeType;

public class TraceConst {
    public static final String TRACE_ID = "traceId";

    public static final MimeType TRACE_ID_MIME_TYPE = MimeType.valueOf("message/x.rsocket.trace.id.v0");
}
