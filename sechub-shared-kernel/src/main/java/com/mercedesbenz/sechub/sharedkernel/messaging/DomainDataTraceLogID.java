// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.sharedkernel.TraceLogID;

public class DomainDataTraceLogID extends TraceLogID<DomainMessagePart> {

    public DomainDataTraceLogID(DomainMessagePart source) {
        super(source);
    }

    public static DomainDataTraceLogID traceLogID(DomainMessagePart data) {
        return new DomainDataTraceLogID(data);
    }

    @Override
    protected String createContent(DomainMessagePart source) throws Exception {
        if (source == null) {
            return null;
        }
        return source.getRaw(MessageDataKeys.SECHUB_JOB_UUID.getId());
    }

}
