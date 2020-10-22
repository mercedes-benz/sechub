package com.daimler.sechub.domain.scan.product.pds;

public abstract class PDSSecHubConfigDataKey {

    private String id;
    private String description;

    PDSSecHubConfigDataKey(String key, String description) {
        this.id = key.toLowerCase();
        this.description = description;
    }
    
    public String getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public abstract boolean isReadFromSecHubExecutor(); 
    public abstract boolean isAlwaysSentToPDS(); 
}