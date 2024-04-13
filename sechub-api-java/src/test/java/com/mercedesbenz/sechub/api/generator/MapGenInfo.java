// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.generator;

import java.util.LinkedHashMap;
import java.util.Map;

class MapGenInfo {
    String targetClassName;
    String targetInternalAccessClassName;
    Class<?> fromGenclazz;
    boolean ignored;
    boolean publicAvailable;
    private Map<String, BeanDataContainer> referenceMap = new LinkedHashMap<>();

    public String toString() {
        return targetInternalAccessClassName + ", ignored=" + ignored;
    }

    public boolean addReference(String beanName, MapGenInfo mapGenForGenClass, TypeInfo typeInfo) {
        Map<String, BeanDataContainer> map = getReferenceMap();
        if (map.containsKey(beanName)) {
            BeanDataContainer foundMapGen = map.get(beanName);

            if (mapGenForGenClass != foundMapGen.mapGenInfo) {
                throw new IllegalStateException(
                        "There exists already a mapping for bean: " + beanName + " with: " + foundMapGen + ", but updated was for: " + mapGenForGenClass);
            }
            ApiWrapperObjectsGenerator.LOG.debug("added reference '{}' in {} already exists", beanName, this);
            return false;
        }
        ApiWrapperObjectsGenerator.LOG.info("added reference '{}' in {} which points to {}", beanName, this, mapGenForGenClass);

        BeanDataContainer container = new BeanDataContainer();
        container.mapGenInfo = mapGenForGenClass;
        container.beanName = beanName;
        container.generatedClassTypeInfo = typeInfo;

        map.put(beanName, container);
        return true;

    }

    public void markPublicAvailable() {
        this.publicAvailable = true;
    }

    public Map<String, BeanDataContainer> getReferenceMap() {
        return referenceMap;
    }

}