// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.commons.core.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.pds.commons.core.PDSJSONConverter;
import com.mercedesbenz.sechub.pds.commons.core.PDSJSONConverterException;

/**
 * Configuration of PDS (product delegation server) - will be read at startup
 * for server
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PDSServerConfiguration {

    private String apiVersion;
    /**
     * Server ID is necessary when sharing same database inside a cluster. For
     * example when a sechub server and all PDS sharing same database. Or when
     * sechub server has it's own DB but all PDS share another one.<br>
     * <br>
     * The serverID is used inside Database
     *
     */
    private String serverId;

    private List<PDSProductSetup> products = new ArrayList<>();

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public List<PDSProductSetup> getProducts() {
        return products;
    }

    public void setProducts(List<PDSProductSetup> work) {
        this.products = work;
    }

    public static PDSServerConfiguration fromJSON(String json) throws PDSJSONConverterException {
        return PDSJSONConverter.get().fromJSON(PDSServerConfiguration.class, json);
    }
}
