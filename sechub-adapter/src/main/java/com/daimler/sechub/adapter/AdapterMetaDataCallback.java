// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

public interface AdapterMetaDataCallback {

    /**
     * Persists given meta data - must be inside a new transaction, so it is ensured
     * that the meta data is stored immediately!
     *
     * @param metaData
     */
    public void persist(AdapterMetaData metaData);

    /**
     * @return meta data or <code>null</code> if no meta data is available
     */
    public AdapterMetaData getMetaDataOrNull();

}
