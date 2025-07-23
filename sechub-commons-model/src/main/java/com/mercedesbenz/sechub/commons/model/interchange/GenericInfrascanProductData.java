package com.mercedesbenz.sechub.commons.model.interchange;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericInfrascanProductData {

    private String product;

    private List<GenericInfrascanFinding> findings = new ArrayList<>();
    
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
    
    public List<GenericInfrascanFinding> getFindings() {
        return findings;
    }
}
