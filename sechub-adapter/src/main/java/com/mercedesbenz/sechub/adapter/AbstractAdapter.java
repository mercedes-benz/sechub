// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import java.util.Map;

import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext.ExecutionType;
import com.mercedesbenz.sechub.adapter.support.APIURLSupport;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

public abstract class AbstractAdapter<A extends AdapterContext<C>, C extends AdapterConfig> implements Adapter<C> {

    private String adapterId;
    private APIURLSupport apiURLSupport;

    protected AbstractAdapter() {
        apiURLSupport = createAPIURLSupport();
    }

    protected JSONAdapterSupport createJsonSupport(TraceIdProvider provider) {
        return new JSONAdapterSupport(this, provider);
    }

    public AdapterCanceledByUserException asAdapterCanceledByUserException(TraceIdProvider provider) {
        return new AdapterCanceledByUserException(getAdapterLogId(provider));
    }

    public AdapterException asAdapterException(String message, TraceIdProvider provider) {
        return asAdapterException(message, null, provider);
    }

    public AdapterException asAdapterException(String message, Throwable t, TraceIdProvider provider) {
        return AdapterException.asAdapterException(getAdapterLogId(provider), message, t);
    }

    /**
     * Assert current thread is not interrupted - this can be done by a cancel or
     * restart operation. If current thread is interrupted, a new adapter exception
     * will be thrown
     *
     * @throws AdapterException
     */
    protected void assertThreadNotInterrupted() throws AdapterException {
        if (Thread.currentThread().isInterrupted()) {
            throw new AdapterException(getAdapterLogId(null), "Execution thread was interrupted");
        }
    }

    @Override
    public final AdapterLogId getAdapterLogId(TraceIdProvider traceIdProvider) {
        if (adapterId == null) {
            adapterId = createAdapterId();
        }
        String traceID = traceIdProvider == null ? null : traceIdProvider.getTraceID();
        return new AdapterLogId(adapterId, traceID);
    }

    protected String createAdapterId() {
        return getClass().getSimpleName();
    }

    @Override
    public final AdapterExecutionResult start(C config, AdapterMetaDataCallback callback) throws AdapterException {
        AdapterRuntimeContext runtimeContext = new AdapterRuntimeContext();
        /*
         * callback is from product executor and resolves adapter meta data for the
         * product result (which is reused on soft restart of job
         */
        runtimeContext.callback = callback;
        runtimeContext.metaData = callback.getMetaDataOrNull();

        if (runtimeContext.metaData == null) {
            /* not reused, we need a complete new PDS job */
            runtimeContext.metaData = new AdapterMetaData();
            runtimeContext.metaData.adapterVersion = getAdapterVersion();
            runtimeContext.type = ExecutionType.INITIAL;

        } else {
            /* reuse existing PDS job means we have a restart */
            runtimeContext.type = ExecutionType.RESTART;

        }
        return execute(config, runtimeContext);
    }

    @Override
    public final boolean cancel(C config, AdapterMetaDataCallback callback) throws AdapterException {
        AdapterMetaData metaData = callback.getMetaDataOrNull();

        if (metaData == null) {
            return false;
        }
        
        metaData.setValue(META_DATA_KEY_PRODUCT_CANCELED, true);
        callback.persist(metaData);

        AdapterRuntimeContext runtimeContext = new AdapterRuntimeContext();
        runtimeContext.callback = null;
        runtimeContext.metaData = metaData;
        runtimeContext.type = ExecutionType.CANCEL;
        

        AdapterExecutionResult result = execute(config, runtimeContext);

        return result.hasBeenCanceled();
    }

    protected abstract AdapterExecutionResult execute(C config, AdapterRuntimeContext runtimeContext) throws AdapterException;

    /**
     * @param api
     * @param config
     * @return api url - will always be like: "baseUrl/prefix/apiCall"
     */
    public final String createAPIURL(String api, C config) {
        return createAPIURL(api, config, (String) null);
    }

    /**
     * @param api
     * @param config
     * @return api url - will always be like: "baseUrl/prefix/apiCall"
     */
    public final String createAPIURL(String api, A adapter) {
        return createAPIURL(api, adapter.getConfig());
    }

    /**
     *
     * @param api
     * @param config
     * @param otherBaseURL
     * @return api url - will always be like: "otherBaseURL/prefix/apiCall"
     */
    public final String createAPIURL(String api, C config, String otherBaseURL) {
        return createAPIURL(api, config, otherBaseURL, null);
    }

    /**
     *
     * @param api
     * @param config
     * @param otherBaseURL
     * @return api url - will always be like: "otherBaseURL/prefix/apiCall"
     */
    public final String createAPIURL(String api, C config, Map<String, String> map) {
        String prefix = getAPIPrefix();
        return internalCreateAPIURL(api, config, prefix, null, map);
    }

    /**
     *
     * @param api
     * @param config
     * @param otherBaseURL
     * @return api url - will always be like: "otherBaseURL/prefix/apiCall"
     */
    public final String createAPIURL(String api, C config, String otherBaseURL, Map<String, String> map) {
        String prefix = getAPIPrefix();
        return internalCreateAPIURL(api, config, prefix, otherBaseURL, map);
    }

    /**
     * Returns the API prefix or <code>null</code> if there is none. <br>
     * <br>
     * Is used to create API urls.<br>
     * An example: host:"localhost:8080", apiCall:"users", prefix:"api/v1.0" would
     * lead to "http://localhost:8080/api/v1.0/users"
     *
     * @return
     */
    protected abstract String getAPIPrefix();

    protected APIURLSupport createAPIURLSupport() {
        return new APIURLSupport();
    }

    private String internalCreateAPIURL(String apiPath, C config, String apiPrefix, String otherBaseURL, Map<String, String> map) {
        return apiURLSupport.createAPIURL(apiPath, config, apiPrefix, otherBaseURL, map);
    }

}
