// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.nessus;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.mercedesbenz.sechub.adapter.AbstractSpringRestAdapterContext;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;

/**
 * Context for NESSUS execution.
 *
 * @author Albert Tregnaghi
 *
 */
public class NessusContext extends AbstractSpringRestAdapterContext<NessusAdapterConfig, NessusAdapter> implements NessusAdapterContext {

    String nessusSessionToken = "";
    String nessusPolicyUID;
    Long nessusScanId;
    private String historyId;
    private String exportFileId;

    public NessusContext(NessusAdapterConfig config, NessusAdapter adapter, AdapterRuntimeContext runtimeContext) {
        super(config, adapter, runtimeContext);
    }

    @Override
    protected ClientHttpRequestInterceptor createInterceptorOrNull(NessusAdapterConfig config) {
        return new NessusClientHttpRequestInterceptor(this);
    }

    @Override
    public String getNessusPolicyUID() {
        return nessusPolicyUID;
    }

    @Override
    public void setNessusPolicyId(String nessusPolicyUID) {
        this.nessusPolicyUID = nessusPolicyUID;
    }

    @Override
    public void setNessusSessionToken(String token) {
        this.nessusSessionToken = token;
    }

    public String getNessusSessionToken() {
        return nessusSessionToken;
    }

    @Override
    public void setNessusScanId(Long scanId) {
        this.nessusScanId = scanId;
    }

    @Override
    public Long getNessusScanId() {
        return nessusScanId;
    }

    @Override
    public String getHistoryId() {
        return historyId;
    }

    @Override
    public void setHistoryId(String id) {
        this.historyId = id;

    }

    public void setExportFileId(String fileId) {
        this.exportFileId = fileId;
    }

    public String getExportFileId() {
        return exportFileId;
    }

}
