// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

public class TestJSONConverterObject {
    String info;
    TestJSONConverterEnum enumValue;

    TestJSONConverterObject() {

    }

    TestJSONConverterObject(String info) {
        this.info = info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setEnumValue(TestJSONConverterEnum enumValue) {
        this.enumValue = enumValue;
    }

    public TestJSONConverterEnum getEnumValue() {
        return enumValue;
    }
}