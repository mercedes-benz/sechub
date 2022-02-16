// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

public class AdapterRuntimeContext {

    AdapterMetaDataCallback callback;
    AdapterMetaData metaData;
    ExecutionType type = ExecutionType.INITIAL;
    boolean stopped;

    public void markStopped() {
        this.stopped = true;
    }

    public ExecutionType getType() {
        return type;
    }

    public AdapterMetaData getMetaData() {
        return metaData;
    }

    public AdapterMetaDataCallback getCallback() {
        return callback;
    }

    public enum ExecutionType {
        INITIAL, RESTART, STOP
    }
}
