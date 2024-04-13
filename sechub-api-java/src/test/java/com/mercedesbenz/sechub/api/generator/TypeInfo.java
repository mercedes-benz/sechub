// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.generator;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

class TypeInfo {

    private boolean asElement;
    private boolean asList;
    private boolean asVoid;
    private Class<?> innerType;
    private String fullTypeAsString;
    private String innerTypeAsString;

    /**
     * Creates a type info about the method return type
     *
     * @param method
     */
    public TypeInfo(Method method) {
        Type genericReturnType = method.getGenericReturnType();

        initByType(genericReturnType);
    }

    public TypeInfo(Parameter parameter) {
        Type genericReturnType = parameter.getParameterizedType();

        initByType(genericReturnType);
    }

    private void initByType(Type genericReturnType) {

        fullTypeAsString = genericReturnType.getTypeName();
        boolean hasGeneric = fullTypeAsString.contains("<");

        asList = fullTypeAsString.startsWith("java.util.List<");
        innerTypeAsString = fullTypeAsString;
        if (hasGeneric) {
            int pos1 = innerTypeAsString.indexOf("<");
            int pos2 = innerTypeAsString.lastIndexOf(">");

            innerTypeAsString = innerTypeAsString.substring(pos1 + 1, pos2);
        }
        asElement = !asList;
        if (Void.TYPE.equals(genericReturnType)) {
            asVoid = true;
        }

    }

    public boolean isAsElement() {
        return asElement;
    }

    public boolean isAsList() {
        return asList;
    }

    public Class<?> getInnerType() {
        if (innerType == null) {
            if (!asVoid) {
                try {
                    innerType = Class.forName(innerTypeAsString);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Was not able to init bean container, because class does not exist!", e);
                }
            }

        }
        return innerType;
    }

    public String getFullTypeAsString() {
        return fullTypeAsString;
    }

    public String getInnerTypeAsString() {
        return innerTypeAsString;
    }

}