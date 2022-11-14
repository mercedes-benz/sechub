// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import java.util.Map;

/**
 * Marker interface for SecHub adapters
 *
 * @author Albert Tregnaghi
 *
 */
public interface Adapter<C extends AdapterConfig> {

    public AdapterLogId getAdapterLogId(TraceIdProvider provider);

    public AdapterCanceledByUserException asAdapterCanceledByUserException(TraceIdProvider provider);

    public AdapterException asAdapterException(String message, TraceIdProvider provider);

    public AdapterException asAdapterException(String message, Throwable t, TraceIdProvider provider);

    public String createAPIURL(String apiPart, C config);

    public String createAPIURL(String apiPart, C config, Map<String, String> map);

    public int getAdapterVersion();

    /**
     * Starts or restarts and returns result
     *
     * @param config
     * @param callback
     * @return result
     * @throws NessusAdapterException
     */
    AdapterExecutionResult start(C config, AdapterMetaDataCallback callback) throws AdapterException;

    /**
     * Triggers cancel returns <code>true</code> when cancel was done
     *
     * @param config
     * @param callback
     * @return <code>true</code> when stop was possible
     */
    boolean cancel(C config, AdapterMetaDataCallback callback) throws AdapterException;
}
