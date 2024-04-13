// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.generator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BeanGeneratorUtil {

    private static String[] beanPrefixes = new String[] { "is", "set", "get" };

    public static String resolveBeanName(Method method) {
        String beanName = null;
        String originMethodName = method.getName();
        for (String replacer : beanPrefixes) {
            if (originMethodName.startsWith(replacer)) {
                beanName = originMethodName.substring(replacer.length());
                break;
            }
        }
        if (beanName != null) {
            if (!Character.isUpperCase(beanName.charAt(0))) {
                return "";
            }
            return beanName;
        } else {
            return "";
        }
    }

    public static String asFieldName(String name) {
        return ("" + name.charAt(0)).toLowerCase() + name.substring(1);
    }

    public static List<Parameter> getParameters(Method method) {
        Parameter[] params = method.getParameters();
        List<Parameter> paramList = Arrays.asList(params);
        return paramList;
    }

    public static List<Method> collectGettersAndSetters(Class<?> fromGenclazz) {
        List<Method> methodsCollected = new ArrayList<>();
        Method[] methods = fromGenclazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            String beanName = resolveBeanName(method);
            if (beanName.isEmpty()) {
                continue;
            }
            methodsCollected.add(method);
        }

        // sort the methods - so generation output always in same ordering
        // (avoids deltas because of random ordering...)
        Collections.sort(methodsCollected, new SimpleMethodComparator());

        return methodsCollected;
    }

    private static class SimpleMethodComparator implements Comparator<Method> {

        @Override
        public int compare(Method o1, Method o2) {
            String object1String = o1.toString();
            String object2String = o2.toString();

            return object1String.compareTo(object2String);
        }

    }

}
