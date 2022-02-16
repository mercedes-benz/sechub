// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.usecase;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.daimler.sechub.docgen.reflections.Reflections;
import com.daimler.sechub.docgen.util.ReflectionsFactory;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;

/**
 * This tests that Steps which are annotated as needing rest api documentation
 * are really documented. This will prevent to forget documentation tests!
 *
 * @author Albert Tregnaghi
 *
 */
public class UsecaseStepsWithRestDocAreDocumentedTest {

    private static final Logger LOG = LoggerFactory.getLogger(UsecaseStepsWithRestDocAreDocumentedTest.class);

    @Test
    public void usecases_having_steps_with_restapi_doc_needed_are_documented_by_restdoc() throws Exception {
        /* prepare */
        TestContext fullData = new TestContext();

        Reflections reflections = ReflectionsFactory.create();

        /* inspect */
        Map<Class<?>, InspData> map = buildMapOfStepsNeedingRestDoc(reflections);
        fetchMethodsDocumentedWithUsecaseRestdocAnnotation(fullData, reflections, map);
        buildProblemsForUsecasesNeedingRestdocButIsMissing(fullData, map);

        /* test */
        if (fullData.isNotOkay()) {
            fail(fullData.toString());
        }
    }

    private static class TestContext {
        StringBuilder problems = new StringBuilder();
        int amountOfmissingRestDocTests;

        public boolean isNotOkay() {
            return problems.length() > 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (amountOfmissingRestDocTests > 0) {
                sb.append(amountOfmissingRestDocTests);
                sb.append(" @UseCaseRestDoc test are missing");
            } else {
                sb.append("No @UseCaseRestDoc tests missing but other problems found");
            }
            sb.append("\nProblem details:");
            sb.append(problems);
            return sb.toString();
        }
    }

    private static class InspData {

        private List<String> stepmessages = new ArrayList<>();
        private Class<?> key;
        private int restDocFound;

        public InspData(Class<?> key) {
            this.key = key;
        }

        public static InspData create(Class<?> key) {
            return new InspData(key);
        }

        public void add(String stepInfo) {
            stepmessages.add(stepInfo);
        }

        @Override
        public String toString() {
            return "InspData [key=" + key + ",\n" + key.getSimpleName() + "\n - restDocFound=" + restDocFound + ",\n - restDocNecessary=" + stepmessages.size()
                    + ",\n - found places needs to be documented=\n" + StringUtils.collectionToDelimitedString(stepmessages, "\n") + "\n]";
        }

        public void restDocFoundInc() {
            restDocFound++;
        }

        public boolean isRestDocMissing() {
            return restDocFound < stepmessages.size();
        }

        public boolean areMoreRestDocsDefinedThanSteps() {
            return restDocFound > stepmessages.size();
        }

    }

    private void buildProblemsForUsecasesNeedingRestdocButIsMissing(TestContext context, Map<Class<?>, InspData> map) {
        for (Class<?> clazz : map.keySet()) {
            InspData data = map.get(clazz);
            if (data.isRestDocMissing()) {
                context.problems.append("\nUsecase must have documented REST API, but at least one @UseCaseRestDoc test ist missing: " + data.toString());
                context.amountOfmissingRestDocTests++;
            } else if (data.areMoreRestDocsDefinedThanSteps()) {
                if (data.stepmessages.size() == 1) {
                    /* just one step with multiple variants - always okay */
                    continue;
                }
                /* This could be problematic - maybe */
                LOG.warn(
                        "More tests annotated with @UseCaseRestDoc for the use case found, than defined in Steps. This can happen when having multiple variations. \n"
                                + "It's not clear if this a problem and how many rest doc tests should be defined, because you have " + data.stepmessages.size()
                                + " annotated steps needing rest doc and there were " + data.restDocFound + " rest doc tests found.\n\n"
                                + "PLease ensure you haven't missed one to describe :" + data.toString());
            }
        }
    }

    private void fetchMethodsDocumentedWithUsecaseRestdocAnnotation(TestContext context, Reflections reflections, Map<Class<?>, InspData> map) {
        Set<Method> restDocAnnotatedMethods = reflections.getMethodsAnnotatedWith(UseCaseRestDoc.class);
        for (Method method : restDocAnnotatedMethods) {
            UseCaseRestDoc restdoc = method.getAnnotation(UseCaseRestDoc.class);
            Class<? extends Annotation> usecaseClass = restdoc.useCase();
            InspData found = map.get(usecaseClass);
            if (found == null) {
                context.problems.append("\nUseCaseRestDoc found, but not tagged inside one of the steps of " + restdoc.useCase());
            } else {
                found.restDocFoundInc();
            }
        }
    }

    private Map<Class<?>, InspData> buildMapOfStepsNeedingRestDoc(Reflections reflections) throws IllegalAccessException, InvocationTargetException {
        Map<Class<?>, InspData> map = new LinkedHashMap<>();
        Set<Class<? extends Annotation>> usesCaseAnnotations = findUseCaseAnnotations(reflections);

        for (Class<? extends Annotation> usecaseClass : usesCaseAnnotations) {
            Set<Method> methodsOfUseCase = reflections.getMethodsAnnotatedWith(usecaseClass);
            for (Method methodOfUseCase : methodsOfUseCase) {
                Annotation annot = methodOfUseCase.getAnnotation(usecaseClass);
                inspectMethodAnnotatedWithUseCase(map, usecaseClass, methodOfUseCase, annot);
            }
        }
        return map;
    }

    private void inspectMethodAnnotatedWithUseCase(Map<Class<?>, InspData> map, Class<? extends Annotation> usecaseClass, Method methodOfUseCase,
            Annotation annot) throws IllegalAccessException, InvocationTargetException {
        for (Method methodInAnnotation : annot.getClass().getDeclaredMethods()) {
            /*
             * each usecase annotation should have a step inside, so we extract STEP
             * information in next steps
             */
            if (Step.class.equals(methodInAnnotation.getReturnType())) {
                /* found method */
                Step step = (Step) methodInAnnotation.invoke(annot);
                if (step.needsRestDoc()) {
                    InspData data = map.computeIfAbsent(usecaseClass, key -> InspData.create(key));
                    data.add(methodOfUseCase.toString());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Class<? extends Annotation>> findUseCaseAnnotations(Reflections reflections) {
        Set<Class<?>> x = reflections.getTypesAnnotatedWith(UseCaseDefinition.class);
        Set<Class<? extends Annotation>> usesCaseAnnotations = new LinkedHashSet<>();
        for (Object y : x) {
            usesCaseAnnotations.add((Class<? extends Annotation>) y);
        }
        return usesCaseAnnotations;
    }

}
