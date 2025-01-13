// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.test.JSONTestUtil.DirectJSonable;

public class TestPDSSupport {

    public List<JSONKeyValue> toKeyValue(Map<String, String> map) {
        List<JSONKeyValue> list = new ArrayList<>();
        for (String key : map.keySet()) {
            String value = map.get(key);
            JSONKeyValue keyValue = new JSONKeyValue();
            keyValue.key = key;
            keyValue.value = value;
            list.add(keyValue);
        }
        return list;
    }

    public static class JSONKeyValue implements DirectJSonable {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + ":" + toJSON();
        }

        @Override
        public String toJSON() {
            return "{ \"key\" : \"" + key + "\", \"value\" : \"" + value + "\"}";
        }

    }
}
