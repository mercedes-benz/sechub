// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.generator;

class BeanDataContainer {

    TypeInfo generatedClassTypeInfo;
    MapGenInfo mapGenInfo;
    String beanName;

    public String asGenerateType() {
        return generatedClassTypeInfo.getFullTypeAsString();
    }

    public String asTargetTypeResult() {
        if (generatedClassTypeInfo.isAsList()) {
            return "List<" + mapGenInfo.targetClassName + ">";
        }
        return mapGenInfo.targetClassName;
    }

    public String asTargetTypeInstance() {
        if (generatedClassTypeInfo.isAsList()) {
            return "ArrayList<>";
        }
        return mapGenInfo.targetClassName;
    }

    public boolean isAsList() {
        return generatedClassTypeInfo.isAsList();
    }

    public String targetClassName() {
        return mapGenInfo.targetClassName;
    }

}