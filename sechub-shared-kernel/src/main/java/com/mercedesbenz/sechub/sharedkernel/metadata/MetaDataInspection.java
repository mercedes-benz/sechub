// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.metadata;

public interface MetaDataInspection {

    public static final String TRACE_ID = "metadata.traceid";

    String getId();

    /**
     * Notice meta data as key value pair
     *
     * @param key
     * @param value
     * @return inspection never <code>null</code>
     */
    MetaDataInspection notice(String key, Object value);

}