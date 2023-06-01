// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.util.UUID;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.mercedesbenz.sechub.adapter.AbstractSpringRestAdapterContext;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport;
import com.mercedesbenz.sechub.adapter.support.RestOperationsSupport;
import com.mercedesbenz.sechub.commons.core.resilience.ResilientActionExecutor;
import com.mercedesbenz.sechub.commons.core.resilience.ResilientRunnableExecutor;
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

    private PDSSocketExceptionResilienceConsultant socketExceptionConsultant;
    private ResilientRunnableExecutor resilientExecutor;
    private ResilientActionExecutor<PDSJobStatus> resilientJobStatusResultExecutor;
    private ResilientActionExecutor<String> resilientStringResultExecutor;

    public PDSContext(PDSAdapterConfig config, PDSAdapter adapter, AdapterRuntimeContext runtimeContext) {
        super(config, adapter, runtimeContext);
        urlBuilder = new PDSUrlBuilder(config.getProductBaseURL());
        jsonSupport = new JSONAdapterSupport(adapter, config);
        restSupport = new RestOperationsSupport(getRestOperations());

        socketExceptionConsultant = new PDSSocketExceptionResilienceConsultant();

        resilientExecutor = new ResilientRunnableExecutor();
        resilientExecutor.add(socketExceptionConsultant);

        resilientJobStatusResultExecutor = new ResilientActionExecutor<>();
        resilientJobStatusResultExecutor.add(socketExceptionConsultant);

        resilientStringResultExecutor = new ResilientActionExecutor<>();
        resilientStringResultExecutor.add(socketExceptionConsultant);

    }

    public PDSSocketExceptionResilienceConsultant getSocketExceptionConsultant() {
        return socketExceptionConsultant;
    }

    public ResilientRunnableExecutor getResilientExecutor() {
        return resilientExecutor;
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
