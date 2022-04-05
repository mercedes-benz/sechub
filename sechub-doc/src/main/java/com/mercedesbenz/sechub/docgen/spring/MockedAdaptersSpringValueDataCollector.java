// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.spring;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

import com.mercedesbenz.sechub.adapter.mock.MockedAdapter;
import com.mercedesbenz.sechub.adapter.mock.MockedAdapterSetupService;
import com.mercedesbenz.sechub.docgen.DocAnnotationData;
import com.mercedesbenz.sechub.docgen.reflections.Reflections;
import com.mercedesbenz.sechub.docgen.util.DocGeneratorUtil;

/**
 * Collector - inspired by
 * https://github.com/de-jcup/code2doc/blob/master/code2doc-core/src/main/java/de/jcup/code2doc/core/internal/collect/TechInfoLinkAnnotationDataCollector.java
 *
 * @author Albert Tregnaghi
 */
public class MockedAdaptersSpringValueDataCollector {

    private Reflections reflections;

    public MockedAdaptersSpringValueDataCollector(Reflections reflections) {
        notNull(reflections, "reflections must not be null!");
        this.reflections = reflections;
    }

    public List<DocAnnotationData> collect() {

        List<DocAnnotationData> list = new ArrayList<>();
        addWithGenerics(reflections.getSubTypesOf(MockedAdapter.class), list);
        addClassOnly(Collections.singleton(MockedAdapterSetupService.class), list);

        return list;

    }

    private void addWithGenerics(@SuppressWarnings("rawtypes") Set<Class<? extends MockedAdapter>> types, List<DocAnnotationData> list) {
        /*
         * messing around with java generics and compiler warnings . so linked hashset
         * as workaround
         */
        addClassOnly(new LinkedHashSet<>(types), list);
    }

    private void addClassOnly(Set<Class<?>> types, List<DocAnnotationData> list) {
        for (Class<?> type : types) {
            Set<Method> methods = buildMethods(type);
            Set<Field> fields = buildFields(type);

            /* handle class annotations */
            Value info0 = type.getDeclaredAnnotation(Value.class);
            if (info0 != null) {
                DocAnnotationData data0 = DocGeneratorUtil.buildDataBy(info0, type);
                data0.linkedClass = type;
                list.add(data0);
            }

            /* handle method annotations */
            for (Method method : methods) {
                Value info1 = method.getDeclaredAnnotation(Value.class);
                if (info1 == null) {
                    continue;
                }
                DocAnnotationData data1 = DocGeneratorUtil.buildDataBy(info1, method);
                data1.linkedMethod = method;

                list.add(data1);
            }

            /* handle field annotations */
            for (Field field : fields) {
                Value info2 = field.getDeclaredAnnotation(Value.class);
                if (info2 == null) {
                    continue;
                }
                DocAnnotationData data2 = DocGeneratorUtil.buildDataBy(info2, field);
                data2.linkedField = field;
                list.add(data2);
            }

        }
    }

    private Set<Field> buildFields(Class<?> adapter) {
        Set<Field> set = new LinkedHashSet<>();
        Field[] fields = adapter.getDeclaredFields();
        for (Field field : fields) {
            Value annotation = field.getAnnotation(Value.class);
            if (annotation != null) {
                set.add(field);
            }
        }
        return set;
    }

    private Set<Method> buildMethods(Class<?> adapter) {
        Set<Method> set = new LinkedHashSet<>();
        Method[] methods = adapter.getDeclaredMethods();
        for (Method method : methods) {
            Value annotation = method.getAnnotation(Value.class);
            if (annotation != null) {
                set.add(method);
            }
        }
        return set;
    }

}