package com.mercedesbenz.sechub.commons.model.interchange;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * SARIF seems to be not a good way for interchange infrascan results, so we
 * decided to create our own interchange result for infrascans.
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericInfrascanResult {

    public static final String ID_GENERIC_INFRASCAN_RESULT = "generic-infrascan-result";
    
    private List<GenericInfrascanProductData> products = new ArrayList<>();
    
    public String getType() {
        return ID_GENERIC_INFRASCAN_RESULT;
    }
    
    public List<GenericInfrascanProductData> getProducts() {
        return products;
    }
    

}
