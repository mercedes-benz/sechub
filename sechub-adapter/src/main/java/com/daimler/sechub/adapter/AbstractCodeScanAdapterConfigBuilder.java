// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.Iterator;
import java.util.Set;

public abstract class AbstractCodeScanAdapterConfigBuilder<B extends AbstractCodeScanAdapterConfigBuilder<B, C>, C extends AbstractCodeScanAdapterConfig>
        extends AbstractAdapterConfigBuilder<B, C> {

    private Set<String> sourceFolders;

    @SuppressWarnings("unchecked")
    public final B setFileSystemSourceFolders(Set<String> sourceFolders) {
        this.sourceFolders = sourceFolders;
        return (B) this;
    }

    @Override
    void packageInternalCustomBuild(C config) {
        if (sourceFolders != null) {
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
            config.sourceScanTargetString = sb.toString();
        }
    }
}