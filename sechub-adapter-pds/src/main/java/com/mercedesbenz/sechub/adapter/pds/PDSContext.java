// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.util.UUID;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.mercedesbenz.sechub.adapter.AbstractSpringRestAdapterContext;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport;
import com.mercedesbenz.sechub.adapter.support.RestOperationsSupport;
import com.mercedesbenz.sechub.commons.core.resilience.ResilientActionExecutor;
import com.mercedesbenz.sechub.commons.core.resilience.ResilientRunOrFailExecutor;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatus;

/**
 * Context for PDS execution.
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSContext extends AbstractSpringRestAdapterContext<PDSAdapterConfig, PDSAdapter> implements PDSAdapterContext {

    private PDSUrlBuilder urlBuilder;
    private JSONAdapterSupport jsonSupport;
    private RestOperationsSupport restSupport;
    private UUID pdsJobUUID;

    private PDSAdapterResilienceConsultant resilienceConsultant;
    private ResilientRunOrFailExecutor resilientRunOrFailExecutor;
    private ResilientActionExecutor<PDSJobStatus> resilientJobStatusResultExecutor;
    private ResilientActionExecutor<String> resilientStringResultExecutor;

    public PDSContext(PDSAdapterConfig config, PDSAdapter adapter, AdapterRuntimeContext runtimeContext) {
        super(config, adapter, runtimeContext);
        urlBuilder = new PDSUrlBuilder(config.getProductBaseURL());
        jsonSupport = new JSONAdapterSupport(adapter, config);
        restSupport = new RestOperationsSupport(getRestOperations());

        resilienceConsultant = new PDSAdapterResilienceConsultant();

        createResilientExectors();
        prepareResilientExecutors();

    }

    void createResilientExectors() {
        resilientRunOrFailExecutor = new ResilientRunOrFailExecutor();
        resilientJobStatusResultExecutor = new ResilientActionExecutor<>();
        resilientStringResultExecutor = new ResilientActionExecutor<>();
    }

    void prepareResilientExecutors() {
        resilientRunOrFailExecutor.add(resilienceConsultant);
        resilientJobStatusResultExecutor.add(resilienceConsultant);
        resilientStringResultExecutor.add(resilienceConsultant);
    }

    public PDSAdapterResilienceConsultant getResilienceConsultant() {
        return resilienceConsultant;
    }

    public ResilientRunOrFailExecutor getResilientRunOrFailExecutor() {
        return resilientRunOrFailExecutor;
    }

    public ResilientActionExecutor<PDSJobStatus> getResilientJobStatusResultExecutor() {
        return resilientJobStatusResultExecutor;
    }

    public ResilientActionExecutor<String> getResilientStringResultExecutor() {
        return resilientStringResultExecutor;
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

    public void setPDSJobUUID(UUID jobUUID) {
        this.pdsJobUUID = jobUUID;
    }

    public UUID getPdsJobUUID() {
        return pdsJobUUID;
    }

    @Override
    protected boolean enableResourceHttpMessageConverterHandlingInputStream() {
        return true;
    }

}
