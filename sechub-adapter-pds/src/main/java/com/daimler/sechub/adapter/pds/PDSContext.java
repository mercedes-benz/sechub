// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.daimler.sechub.adapter.AbstractSpringRestAdapterContext;
import com.daimler.sechub.adapter.AdapterRuntimeContext;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;
import com.daimler.sechub.adapter.support.RestOperationsSupport;

/**
 * Context for NETSPARKER execution.
 * 
 * @author Albert Tregnaghi
 *
 */
public class PDSContext extends AbstractSpringRestAdapterContext<PDSAdapterConfig, PDSAdapter> implements PDSAdapterContext {

    private PDSUrlBuilder urlBuilder;
    private JSONAdapterSupport jsonSupport;
    private RestOperationsSupport restSupport;
    private String pdsJobUUID;

    public PDSContext(PDSAdapterConfig config, PDSAdapter adapter, AdapterRuntimeContext runtimeContext) {
        super(config, adapter, runtimeContext);
        urlBuilder = new PDSUrlBuilder(config.getProductBaseURL());
        jsonSupport = new JSONAdapterSupport(adapter, config);
        restSupport = new RestOperationsSupport(getRestOperations());
    }

    public RestOperationsSupport getRestSupport() {
        return restSupport;
    }

    public PDSUrlBuilder getUrlBuilder() {
        return urlBuilder;
    }

    public JSONAdapterSupport getJsonSupport() {
        return jsonSupport;
    }

    @Override
    protected ClientHttpRequestInterceptor createInterceptorOrNull(PDSAdapterConfig config) {
        return new PDSClientHttpRequestInterceptor(config);
    }

    public void setPDSJobUUID(String jobUUID) {
        this.pdsJobUUID = jobUUID;
    }

    public String getPdsJobUUID() {
        return pdsJobUUID;
    }

}
