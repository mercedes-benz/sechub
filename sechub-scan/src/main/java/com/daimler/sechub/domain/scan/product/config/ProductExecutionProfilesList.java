// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.commons.model.JSONable;

public class ProductExecutionProfilesList implements JSONable<ProductExecutionProfilesList>{
    
    private static final ProductExecutionProfilesList IMPORTER = new ProductExecutionProfilesList();

    private List<ProductExecutionProfileListEntry> executionProfiles = new ArrayList<>();

    private String type = "executionProfileList";
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public List<ProductExecutionProfileListEntry> getExecutionProfiles() {
        return executionProfiles;
    }

    public void setExecutionProfiles(List<ProductExecutionProfileListEntry> executorConfigurations) {
        this.executionProfiles = executorConfigurations;
    }

    @Override
    public Class<ProductExecutionProfilesList> getJSONTargetClass() {
        return ProductExecutionProfilesList.class;
    }
    
    public static ProductExecutionProfilesList fromJSONString(String json) {
        return IMPORTER.fromJSON(json);
    }
}
