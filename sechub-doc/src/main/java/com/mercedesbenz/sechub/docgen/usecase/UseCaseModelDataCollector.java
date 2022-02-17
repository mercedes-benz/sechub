// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.usecase;

import static com.mercedesbenz.sechub.docgen.GeneratorConstants.*;
import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.docgen.reflections.Reflections;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseModelType;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;

/**
 * Collector - inspired by
 * https://github.com/de-jcup/code2doc/blob/master/code2doc-core/src/main/java/de/jcup/code2doc/core/internal/collect/TechInfoLinkAnnotationDataCollector.java
 *
 * @author Albert Tregnaghi
 */
public class UseCaseModelDataCollector {

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseModelDataCollector.class);

    private Reflections reflections;

    public UseCaseModelDataCollector(Reflections reflections) {
        notNull(reflections, "reflections must not be null!");
        this.reflections = reflections;
    }

    @SuppressWarnings("unchecked")
    public UseCaseModel collect() {
        if (DEBUG) {
            LOG.info("start collecting");
        }
        /* @formatter:off
		   generation is done into /src/docs/documents/gen/...
		   included parts are from /src/docs/documents/code2doc/...
		   so we use                        /../code2doc as path
		   @formatter:on */

        UseCaseModel model = new UseCaseModel("../code2doc/usecases", UseCaseModelType.SECHUB);
        Set<Class<?>> usesCaseAnnotations = reflections.getTypesAnnotatedWith(UseCaseDefinition.class);
        if (DEBUG) {
            LOG.info("> will collect for:{} - {}", usesCaseAnnotations.size(), usesCaseAnnotations);
        }
        for (Class<?> useCaseAnnotationClazz : usesCaseAnnotations) {
            collectAnnotationInfo(model, (Class<Annotation>) useCaseAnnotationClazz);
        }
        return model;
    }

    private <T extends Annotation> void collectAnnotationInfo(UseCaseModel model, Class<T> useCaseAnnotationClazz) {
        if (DEBUG) {
            LOG.info("start collecting annotation info:{}", useCaseAnnotationClazz);
        }

        Set<Method> methodsAnnotated = reflections.getMethodsAnnotatedWith(useCaseAnnotationClazz);
        if (DEBUG) {
            LOG.info("found methods annotated with:{} - {}", useCaseAnnotationClazz, methodsAnnotated);
        }

        for (Method method : methodsAnnotated) {
            List<RolesAllowed> rolesAllowed = fetchAllAllowedRoles(method);

            T[] annosFound = method.getAnnotationsByType(useCaseAnnotationClazz);
            if (DEBUG) {
                LOG.info("inspect method:{}\n - roles found:{}\n - annos found:{}", method, rolesAllowed, annosFound);
            }
            for (T anno : annosFound) {
                UseCaseEntry useCaseEntry = model.ensureUseCase(anno.getClass());
                try {
                    Method valueMethod = anno.getClass().getMethod("value");
                    Object value = valueMethod.invoke(anno);
                    if (value instanceof Step) {
                        String location = "method `" + method.getName() + "` in class `" + method.getDeclaringClass().getName() + "`";
                        useCaseEntry.addStep((Step) value, rolesAllowed, location);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(e);

                }
            }
        }
    }

    private List<RolesAllowed> fetchAllAllowedRoles(Method method) {
        RolesAllowed rolesAllowedByMethod = method.getAnnotation(RolesAllowed.class);
        RolesAllowed rolesAllowedByClass = method.getDeclaringClass().getAnnotation(RolesAllowed.class);
        List<RolesAllowed> rolesAllowed = new ArrayList<>();
        if (rolesAllowedByClass != null) {
            rolesAllowed.add(rolesAllowedByClass);
        }
        if (rolesAllowedByMethod != null) {
            rolesAllowed.add(rolesAllowedByMethod);
        }
        return rolesAllowed;
    }

    static String createDomainPartName(Method method) {
        return method.getDeclaringClass().getSimpleName();
    }

}