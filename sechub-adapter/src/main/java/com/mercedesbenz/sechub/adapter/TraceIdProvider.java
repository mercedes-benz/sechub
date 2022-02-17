// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

public interface TraceIdProvider {

    /**
     * @return a trace ID, never <code>null</code>
     */
    String getTraceID();

}
