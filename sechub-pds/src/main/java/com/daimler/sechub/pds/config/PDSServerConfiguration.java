package com.daimler.sechub.pds.config;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.pds.PDSJSONConverter;
import com.daimler.sechub.pds.PDSJSONConverterException;

/**
 * Configuration of PDS (product delegation server) - will be read at startup
 * for server
 * 
 * @author Albert Tregnaghi
 *
 */
public class PDSServerConfiguration {

    private String apiVersion;
    private List<PDSProductSetup> products = new ArrayList<>();
    
    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
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
