// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport;

public class AdapterMetaDataConverter {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterMetaDataConverter.class);

    public AdapterMetaData convertToMetaDataOrNull(String metaDataString) {
        try {
            return JSONAdapterSupport.FOR_UNKNOWN_ADAPTER.fromJSON(AdapterMetaData.class, metaDataString);
        } catch (AdapterException e) {
            LOG.error("Not able to convert to metadata:{}", metaDataString, e);
            return null;
        }
    }

    public String convertToJSONOrNull(AdapterMetaData metaData) {
        if (metaData == null) {
            return null;
        }
        try {
            return JSONAdapterSupport.FOR_UNKNOWN_ADAPTER.toJSON(metaData);
        } catch (AdapterException e) {
            LOG.error("Not able to convert to metadata string:{}", metaData, e);
            return null;
        }
    }
}
