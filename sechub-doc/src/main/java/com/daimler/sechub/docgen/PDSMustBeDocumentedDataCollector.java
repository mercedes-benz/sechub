// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.daimler.sechub.docgen.reflections.Reflections;
import com.daimler.sechub.docgen.util.DocGeneratorUtil;
import com.daimler.sechub.pds.PDSMustBeDocumented;

/**
 * Collector - inspired by
 * https://github.com/de-jcup/code2doc/blob/master/code2doc-core/src/main/java/de/jcup/code2doc/core/internal/collect/TechInfoLinkAnnotationDataCollector.java
 * 
 * @author Albert Tregnaghi
 */
public class PDSMustBeDocumentedDataCollector {

    private Reflections reflections;

    public PDSMustBeDocumentedDataCollector(Reflections reflections) {
        notNull(reflections, "reflections must not be null!");
        this.reflections = reflections;
    }

    public List<DocAnnotationData> collect() {

        Class<? extends Annotation> annotation = PDSMustBeDocumented.class;

        Set<Class<?>> types = reflections.getTypesAnnotatedWith(annotation);
        Set<Method> methods = reflections.getMethodsAnnotatedWith(annotation);
        Set<Field> fields = reflections.getFieldsAnnotatedWith(annotation);

        List<DocAnnotationData> list = new ArrayList<>();
        /* handle class annotations */
        for (Class<?> type : types) {
            PDSMustBeDocumented info = type.getDeclaredAnnotation(PDSMustBeDocumented.class);
            if (info == null) {
                continue;
            }
            DocAnnotationData data = DocGeneratorUtil.buildDataForPDSMustBeDocumented(info, type);
            data.linkedClass = type;
            list.add(data);
        }

        /* handle method annotations */
        for (Method method : methods) {
            PDSMustBeDocumented info = method.getDeclaredAnnotation(PDSMustBeDocumented.class);
            if (info == null) {
                continue;
            }
            DocAnnotationData data = DocGeneratorUtil.buildDataForPDSMustBeDocumented(info, method);
            data.linkedMethod = method;

            list.add(data);
        }

        /* handle field annotations */
        for (Field field : fields) {
            PDSMustBeDocumented info = field.getDeclaredAnnotation(PDSMustBeDocumented.class);
            if (info == null) {
                continue;
            }
            DocAnnotationData data = DocGeneratorUtil.buildDataForPDSMustBeDocumented(info, field);
            data.linkedField = field;
            list.add(data);
        }

        return list;

    }

}