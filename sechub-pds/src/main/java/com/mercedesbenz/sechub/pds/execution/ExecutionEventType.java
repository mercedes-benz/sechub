package com.mercedesbenz.sechub.pds.execution;

public enum ExecutionEventType {

    CANCEL_REQUESTED("event.type.cancel.requested");

    private String id;

    ExecutionEventType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
