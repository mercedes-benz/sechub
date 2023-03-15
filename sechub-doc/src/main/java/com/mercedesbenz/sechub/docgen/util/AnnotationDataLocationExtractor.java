// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.mercedesbenz.sechub.docgen.DocAnnotationData;

public class AnnotationDataLocationExtractor {

    public String extractLocation(DocAnnotationData data) {
        if (data == null) {
            return "";
        }
        Method method = data.linkedMethod;
        if (method != null) {
            return createMethodIdentifier(method);
        }
        Field field = data.linkedField;
        if (field != null) {
            return createFieldIdentifier(field);
        }

        Class<?> clazz = data.linkedClass;
        if (clazz != null) {
            return createClassIdentifier(clazz);
        }
        return "";
    }

    protected String createClassIdentifier(Class<?> clazz) {
        return "Class:" + getSafeClassName(clazz);
    }

    protected String createFieldIdentifier(Field method) {
        return "Field:" + getSafeClassName(method.getDeclaringClass()) + "." + getSafeFieldName(method);
    }

    protected String createMethodIdentifier(Method method) {
        return "Method:" + getSafeClassName(method.getDeclaringClass()) + "#" + getSafeMethodName(method);
    }

    private String getSafeClassName(Class<?> clazz) {
        if (clazz == null) {
            return "null";
        }
        return clazz.getSimpleName();
    }

    private String getSafeMethodName(Method method) {
        if (method == null) {
            return "null";
        }
        return method.getName();
    }

    private String getSafeFieldName(Field field) {
        if (field == null) {
            return "null";
        }
        return field.getName();
    }
}
