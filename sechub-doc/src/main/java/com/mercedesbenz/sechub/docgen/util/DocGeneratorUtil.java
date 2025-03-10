// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.util;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.scheduling.annotation.Scheduled;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.docgen.ConfigurationPropertiesData;
import com.mercedesbenz.sechub.docgen.DocAnnotationData;
import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;

public class DocGeneratorUtil {

    private DocGeneratorUtil() {

    }

    public static String convertCamelCaseToKebabCase(String camelCase) {
        if (camelCase == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        int pos = 0;
        for (char c : camelCase.toCharArray()) {

            boolean upperCase = Character.isUpperCase(c);
            // Spring Boot 3.x does not handle "oAuth2 as o-auth2" but as "oauth2"
            // It was not clear which Util class does this correctly
            // e.g. Spring method inside ParsingUtil doing kebab case conversion
            // but behaves different - so we must handle this different here
            // we only add hyphens when pos greater than 2
            boolean hypenNecessary = pos > 2;
            if (hypenNecessary && upperCase) {
                sb.append('-');
            }
            sb.append(c);
            pos++;
        }

        return sb.toString().toLowerCase();

    }

    /**
     * Converts a spring boot system property to a environment variable name
     *
     * @param systemPropert property name
     * @return environment variable name or <code>null</code> if systemProperty
     *         parameter was <code>null</code>
     */
    public static String convertSystemPropertyToEnvironmentVariable(String systemProperty) {
        if (systemProperty == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : systemProperty.toCharArray()) {

            switch (c) {
            case '.':
                sb.append("_");
                break;
            case '-':
                /* - will not be appended */
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString().toUpperCase();

    }

    public static String convertToIdentifier(String text) {
        if (text == null) {
            return "null";
        }
        return text.replaceAll(" ", "_").toLowerCase();
    }

    public static DocAnnotationData buildDataForMustBeDocumented(MustBeDocumented info, AnnotatedElement element) {
        DocAnnotationData data = new DocAnnotationData();
        data.scope = ensureSafeScope(info.scope());
        data.isSecret = info.secret();
        data.description = info.value();
        buildSpringValueParts(data, element);
        buildSpringScheduledParts(data, element);
        buildSpringConfigPropertiesParts(data, element);

        return data;
    }

    private static String ensureSafeScope(String scope) {
        if (scope == null) {
            return "";
        }
        return scope;
    }

    public static DocAnnotationData buildDataForPDSMustBeDocumented(PDSMustBeDocumented info, AnnotatedElement element) {
        DocAnnotationData data = new DocAnnotationData();
        data.scope = ensureSafeScope(info.scope());
        data.isSecret = info.secret();
        data.description = info.value();
        buildSpringValueParts(data, element);
        buildSpringScheduledParts(data, element);

        return data;
    }

    public static void newLine(StringBuilder sb, String text) {
        sb.append(text);
        sb.append("\n");
    }

    /*
     * TODO Albert Tregnaghi, 2025-02-26: this should be removed/changed. Reason:
     * scope is calculated
     */
    public static DocAnnotationData buildDataBy(Value info, AnnotatedElement element) {
        DocAnnotationData data = new DocAnnotationData();
        buildSpringValueParts(data, element);
        buildSpringScheduledParts(data, element);

        data.description = "See " + fetchClass(element).getSimpleName() + ".java";
        data.scope = ensureSafeScope(toCamelOne(fetchClass(element)).toLowerCase());
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

    static void buildSpringConfigPropertiesParts(DocAnnotationData data, AnnotatedElement element) {
        ConfigurationProperties value = element.getDeclaredAnnotation(ConfigurationProperties.class);
        if (value == null) {
            return;
        }
        data.propertiesData = createConfigurationPropertiesData(element, value);
    }

    public static ConfigurationPropertiesData createConfigurationPropertiesData(AnnotatedElement element, ConfigurationProperties value) {
        ConfigurationPropertiesData pd = null;
        if (element instanceof Class) {
            pd = new ConfigurationPropertiesData();
            Class<?> clazz = (Class<?>) element;
            Constructor<?> boundConstructor = findConstructorToUseOrNull(clazz);

            pd.properties = value;
            pd.propertiesClass = clazz;
            pd.constructor = boundConstructor;
        } else {
            throw new IllegalStateException("When the annotation " + MustBeDocumented.class.getSimpleName() + " shall be used in conjunction with "
                    + ConfigurationProperties.class.getSimpleName() + " it must be defined at class level! But was on:" + element);
        }
        return pd;
    }

    private static Constructor<?> findConstructorToUseOrNull(Class<?> clazz) {
        if (clazz.getName().startsWith("java.")) {
            return null;
        }
        Constructor<?> constructor = resolveBoundConstructor(clazz);
        if (constructor == null) {
            constructor = resolveFirstConstructorWithParameters(clazz);
        }
        return constructor;
    }

    private static Constructor<?> resolveFirstConstructorWithParameters(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            Parameter[] parameters = constructor.getParameters();
            if (parameters.length > 0) {
                return constructor;
            }
        }
        return null;
    }

    private static Constructor<?> resolveBoundConstructor(Class<?> clazz) {
        Constructor<?> boundConstructor = null;
        for (Constructor<?> constructor : clazz.getConstructors()) {
            ConstructorBinding bindingAnnotation = constructor.getDeclaredAnnotation(ConstructorBinding.class);
            if (bindingAnnotation == null) {
                continue;
            }
            boundConstructor = constructor;
            break;
        }
        return boundConstructor;
    }

    static void buildSpringScheduledParts(DocAnnotationData data, AnnotatedElement element) {
        Scheduled scheduled = element.getDeclaredAnnotation(Scheduled.class);
        if (scheduled == null) {
            return;
        }
        data.springScheduled = scheduled;
    }
}
