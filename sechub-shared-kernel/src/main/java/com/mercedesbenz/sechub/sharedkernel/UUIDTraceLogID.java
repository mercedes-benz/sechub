// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import java.util.UUID;

public class UUIDTraceLogID extends TraceLogID<UUID> {

    public UUIDTraceLogID(UUID uuid) {
        super(uuid);
    }

    public static UUIDTraceLogID traceLogID(UUID uuid) {
        return new UUIDTraceLogID(uuid);
    }

    @Override
    protected String createContent(UUID uuid) throws Exception {
        if (uuid == null) {
            return null;
        }
        return uuid.toString();
    }

}
