// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

public class AdapterRuntimeContext {

    AdapterMetaDataCallback callback;
    AdapterMetaData metaData;
    ExecutionType type = ExecutionType.INITIAL;

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
        INITIAL,

        RESTART,

        CANCEL
    }
}
