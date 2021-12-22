package com.daimler.sechub.adapter;

public interface CodeScanAdapterConfig extends AdapterConfig {
    /**
     * Returns a target string. When configured target is one or more folder paths.
     * 
     * @return target URI string or <code>null</code> if none defined
     */
    String getTargetAsString();
}
