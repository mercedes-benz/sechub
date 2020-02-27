package com.daimler.sechub.adapter;

public interface AdapterMetaDataCallback {

    /**
     * Persists given meta data
     * @param metaData
     */
    public void persist(AdapterMetaData metaData);
    
    /**
     * @return meta data or <code>null</code> if no meta data is available
     */
    public AdapterMetaData getMetaDataOrNull();

}
