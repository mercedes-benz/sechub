// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import java.util.Iterator;
import java.util.Set;

public abstract class AbstractCodeScanAdapterConfigBuilder<B extends AbstractCodeScanAdapterConfigBuilder<B, C>, C extends CodeScanAdapterConfig>
        extends AbstractAdapterConfigBuilder<B, C> {

    private Set<String> sourceFolders;

    /**
     * Shall only be used where necessary. We build the
     * <code>sourceScanTargetString</code> by this information which is used only by
     * mocked adapters for integration test. For PDS executions we do not mock at
     * all.
     *
     * @param sourceFolders
     * @return
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public final B setFileSystemSourceFolders(Set<String> sourceFolders) {
        this.sourceFolders = sourceFolders;
        return (B) this;
    }

    @SuppressWarnings("deprecation")
    @Override
    void packageInternalCustomBuild(C config) {
        if (sourceFolders == null) {
            return;
        }
        /**
         * Important: For code scans we provide the information which folders were
         * added/used inside the code scan . At integration tests the target id is used
         * to determine correct mock adapter behavior.
         */
        if (!(config instanceof AbstractCodeScanAdapterConfig)) {
            throw new IllegalArgumentException("Wrong config type class hierarchy. Your config is of type " + config.getClass().getName()
                    + " is not a descendant of " + AbstractCodeScanAdapterConfig.class.getSimpleName());
        }

        AbstractCodeScanAdapterConfig abstractCodeScanConfig = (AbstractCodeScanAdapterConfig) config;

        StringBuilder sb = new StringBuilder();
        for (Iterator<String> it = sourceFolders.iterator(); it.hasNext();) {
            String folder = it.next();
            if (folder == null) {
                continue;
            }
            sb.append(folder);
            if (it.hasNext()) {
                sb.append(';');
            }
        }

        abstractCodeScanConfig.sourceScanTargetString = sb.toString();
    }
}