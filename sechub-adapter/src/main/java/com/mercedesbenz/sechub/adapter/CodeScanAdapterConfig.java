package com.mercedesbenz.sechub.adapter;

public interface CodeScanAdapterConfig extends AdapterConfig {
    /**
     * Returns a target string. When configured target is one or more folder paths.
     *
     * For example, the folder paths are <code>src/java/</code> and
     * <code>src/groovy</code>. In this case, the method returns the target
     * <code>src/java/;src/groovy</code> as string.
     *
     * @return target URI string or <code>null</code> if none defined
     */
    String getTargetAsString();
}
