// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.metadata;

import java.util.Map;
import java.util.TreeMap;

public class MapStorageMetaDataInspection implements MetaDataInspection {

    private String id;
    private Map<String, Object> data = new TreeMap<>();

    public MapStorageMetaDataInspection(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public Map<String, Object> getData() {
        return data;
    }

    @Override
    public MetaDataInspection notice(String key, Object value) {
        if (key == null) {
            return this;
        }
        data.put(key, value);
        return this;
    }

}
