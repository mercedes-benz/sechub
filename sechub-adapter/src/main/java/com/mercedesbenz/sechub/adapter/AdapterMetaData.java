// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mercedesbenz.sechub.commons.model.MetaDataModel;

/**
 * Represents meta data for an adapter - e.g. for restarts of a former adapter
 * execution after a JVM crash.
 *
 * @author Albert Tregnaghi
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.NON_PRIVATE)
@JsonPropertyOrder(alphabetic = true)
public class AdapterMetaData extends MetaDataModel {

    int adapterVersion;

    public int getAdapterVersion() {
        return adapterVersion;
    }

    @Override
    public String toString() {
        return "AdapterMetaData [adapterVersion=" + adapterVersion + ", metaData=" + metaData + "]";
    }

}
