package com.mercedesbenz.sechub.pds.execution;

public enum ExecutionEventDetailIdentifier {

    CANCEL_REQUEST_SECONDS_TO_WAIT_FOR_PROCESS("cancel.request.seconds.wait.process"),

    CANCEL_REQUEST_MILLSECONDS_FOR_CHECK_INTERVAL("cancel.request.milliseconds.check.interval"),

    EVENT_TYPE("event.type"),

    ;

    private String detailId;

    ExecutionEventDetailIdentifier(String id) {
        this.detailId = id;
    }

    public String getDetailId() {
        return detailId;
    }

}
