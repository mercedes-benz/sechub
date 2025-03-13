// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.docgen.reflections.Reflections;
import com.mercedesbenz.sechub.docgen.util.DocGeneratorUtil;

/**
 * Collector - inspired by
 * https://github.com/de-jcup/code2doc/blob/master/code2doc-core/src/main/java/de/jcup/code2doc/core/internal/collect/TechInfoLinkAnnotationDataCollector.java
 *
 * @author Albert Tregnaghi
 */
public class MustBeDocumentedDataCollector {

    private Reflections reflections;

    public MustBeDocumentedDataCollector(Reflections reflections) {
        notNull(reflections, "reflections must not be null!");
        this.reflections = reflections;
    }

    public List<DocAnnotationData> collect() {

        Class<? extends Annotation> annotation = MustBeDocumented.class;

        Set<Class<?>> types = reflections.getTypesAnnotatedWith(annotation);
        Set<Method> methods = reflections.getMethodsAnnotatedWith(annotation);
        Set<Field> fields = reflections.getFieldsAnnotatedWith(annotation);

        List<DocAnnotationData> list = new ArrayList<>();
        /* handle class annotations */
        for (Class<?> type : types) {
            MustBeDocumented info = type.getDeclaredAnnotation(MustBeDocumented.class);
            if (info == null) {
                continue;
            }
            DocAnnotationData data = DocGeneratorUtil.buildDataForMustBeDocumented(info, type);
            data.linkedClass = type;
            list.add(data);
        }

        /* handle method annotations */
        for (Method method : methods) {
            MustBeDocumented info = method.getDeclaredAnnotation(MustBeDocumented.class);
            if (info == null) {
                continue;
            }
            DocAnnotationData data = DocGeneratorUtil.buildDataForMustBeDocumented(info, method);
            data.linkedMethod = method;

            list.add(data);
        }

        /* handle field annotations */
        for (Field field : fields) {
            MustBeDocumented info = field.getDeclaredAnnotation(MustBeDocumented.class);
            if (info == null) {
                continue;
            }
            DocAnnotationData data = DocGeneratorUtil.buildDataForMustBeDocumented(info, field);
            data.linkedField = field;
            list.add(data);
        }

        return list;

    }

}