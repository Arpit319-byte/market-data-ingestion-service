package com.example.stockanalyzer.marketdata.logging;

import java.util.UUID;

import org.slf4j.MDC;

public final class IncidentContext implements AutoCloseable {

    public static final String REQUEST_ID = "requestId";
    public static final String OPERATION = "operation";
    public static final String JOB_ID = "jobId";

    private IncidentContext() {
    }

    public static IncidentContext open(String operation) {
        MDC.put(REQUEST_ID, UUID.randomUUID().toString());
        MDC.put(OPERATION, operation);
        return new IncidentContext();
    }

    public static void putJobId(Long jobId) {
        if (jobId != null) {
            MDC.put(JOB_ID, jobId.toString());
        }
    }

    public static String requestId() {
        return MDC.get(REQUEST_ID);
    }

    @Override
    public void close() {
        MDC.remove(REQUEST_ID);
        MDC.remove(OPERATION);
        MDC.remove(JOB_ID);
    }
}
