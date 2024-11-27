// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.util;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import com.mercedesbenz.sechub.docgen.DocAnnotationData;
import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

public class DocGeneratorUtil {

    private DocGeneratorUtil() {

    }

    public static DocAnnotationData buildDataForMustBeDocumented(MustBeDocumented info, AnnotatedElement element) {
        DocAnnotationData data = new DocAnnotationData();
        data.scope = info.scope();
        data.isSecret = info.secret();
        data.description = info.value();
        buildSpringValueParts(data, element);
        buildSpringScheduledParts(data, element);

        /* when class name shall be used... */
        if (DocumentationScopeConstants.SCOPE_USE_DEFINED_CLASSNAME_LOWERCASED.equals(data.scope)) {
            data.scope = toCamelOne(fetchClass(element)).toLowerCase();
        }
        return data;
    }

    public static DocAnnotationData buildDataForPDSMustBeDocumented(PDSMustBeDocumented info, AnnotatedElement element) {
        DocAnnotationData data = new DocAnnotationData();
        data.scope = info.scope();
        data.isSecret = info.secret();
        data.description = info.value();
        buildSpringValueParts(data, element);
        buildSpringScheduledParts(data, element);

        /* when class name shall be used... */
        if (DocumentationScopeConstants.SCOPE_USE_DEFINED_CLASSNAME_LOWERCASED.equals(data.scope)) {
            data.scope = toCamelOne(fetchClass(element)).toLowerCase();
        }
        return data;
    }

    public static void newLine(StringBuilder sb, String text) {
        sb.append(text);
        sb.append("\n");
    }

    public static DocAnnotationData buildDataBy(Value info, AnnotatedElement element) {
        DocAnnotationData data = new DocAnnotationData();
        buildSpringValueParts(data, element);
        buildSpringScheduledParts(data, element);
        data.description = "See " + fetchClass(element).getSimpleName() + ".java";
        data.scope = toCamelOne(fetchClass(element)).toLowerCase();
        return data;
    }

    static Class<?> fetchClass(AnnotatedElement element) {
        if (element instanceof Class) {
            Class<?> clazz = (Class<?>) element;
            return clazz;
        } else if (element instanceof Method) {
            Method method = (Method) element;
            return method.getDeclaringClass();
        } else if (element instanceof Field) {
            Field field = (Field) element;
            return field.getDeclaringClass();
        }
        /*
         * worst case should never happen... at least do error output and return the
         * annotation class itself
         */
        /* NOSONAR */System.err.println("fetch class not possible for element:" + element + ".\nFallback to element class itself!");
        return element.getClass();
    }

    /**
     * Reduces string to second upper cased char - e.g. "NetsparkerInstallSetupImpl"
     * would be replaced to "Netsparker". Having multiple upper case letters at the
     * beginning will keep all upper cased parts. E.g. "SMTPServerConfiguration"
     * will become "SMTPServer"
     *
     * @param clazz
     * @return string, never <code>null</code>
     */
    public static String toCamelOne(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();

        String clazzName = clazz.getSimpleName();
        boolean first = true;
        boolean atLeastOneLowerCaseFound = false;
        for (char c : clazzName.toCharArray()) {
            if (first) {
                first = false;
            } else {
                boolean upperCase = Character.isUpperCase(c);
                if (!atLeastOneLowerCaseFound) {
                    atLeastOneLowerCaseFound = !upperCase;
                }
                if (upperCase && atLeastOneLowerCaseFound) {
                    break;
                }
            }
            sb.append(c);
        }

        return sb.toString();
    }

    static void buildSpringValueParts(DocAnnotationData data, AnnotatedElement element) {
        Value value = element.getDeclaredAnnotation(Value.class);
        if (value == null) {
            return;
        }
        data.springValue = value.value();
    }

    static void buildSpringScheduledParts(DocAnnotationData data, AnnotatedElement element) {
        Scheduled scheduled = element.getDeclaredAnnotation(Scheduled.class);
        if (scheduled == null) {
            return;
        }
        data.springScheduled = scheduled;
    }
}
