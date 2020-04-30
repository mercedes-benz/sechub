package com.daimler.sechub.sharedkernel;

public interface Abandonable {
    
    /**
     * @return <code>true</code>when progress has been abandonded
     */
    public boolean isAbandoned();
}
