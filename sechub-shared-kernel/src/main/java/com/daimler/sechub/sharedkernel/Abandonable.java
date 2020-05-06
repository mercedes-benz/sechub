package com.daimler.sechub.sharedkernel;

public interface Abandonable {
    
    /**
     * @return <code>true</code>when instance has been abandonded
     */
    public boolean isAbandoned();
}
