package com.mercedesbenz.sechub.pds.execution;

public enum ExecutionEventDetailIdentifier {

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
