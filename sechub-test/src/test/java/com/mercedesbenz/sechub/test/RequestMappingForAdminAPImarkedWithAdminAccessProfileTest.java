// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mercedesbenz.sechub.docgen.reflections.Reflections;
import com.mercedesbenz.sechub.docgen.util.ReflectionsFactory;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;

/**
 * This tests that SecHub REST API for administrators is protected by special
 * profile, so operators must explicit start a server with this profile enabled
 * to have a server instance with such high privileges. are really documented.
 * This will prevent to forget documentation tests!
 *
 * @author Albert Tregnaghi
 *
 */
public class RequestMappingForAdminAPImarkedWithAdminAccessProfileTest {

    @Test
    public void usecases_having_steps_with_restapi_doc_needed_are_documented_by_restdoc() throws Exception {
        /* prepare */
        TestContext fullData = new TestContext();

        Reflections reflections = ReflectionsFactory.create();

        /* inspect */
        Map<Class<?>, InspData> map = buildMapOfStepsNeedingRestDoc(reflections);
        buildProblemsForMissingAdminAcessProfile(fullData, map);

        /* test */
        if (fullData.isNotOkay()) {
            fail(fullData.toString());
        }

    }

    private static class TestContext {
        StringBuilder problems = new StringBuilder();
        int amountOfMissingAdminAPIProfileTags;

        public boolean isNotOkay() {
            return problems.length() > 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (amountOfMissingAdminAPIProfileTags > 0) {
                sb.append(amountOfMissingAdminAPIProfileTags);
                sb.append(" Some admin api parts are not marked with ADMIN_ACCESS profile!");
            } else {
                sb.append("Some other problems found");
            }
            sb.append("\nProblem details:");
            sb.append(problems);
            return sb.toString();
        }
    }

    private static class InspData {

        private Class<?> key;
        private List<String> problems = new ArrayList<>();
        private boolean missingAdminAccess;

        public static InspData create(Class<?> key) {
            InspData data = new InspData();
            data.key = key;

            return data;
        }

        @SuppressWarnings("unused")
        public Class<?> getKey() {
            return key;
        }

        public boolean isAdminAPIDetectedWithoutAdminAccessProfile() {
            return missingAdminAccess;
        }

        public void addNameConventionProblem(String message) {
            problems.add("Name convention problem:" + message);
        }

        public void addMissingAdminAccess(String message) {
            problems.add(message);
            missingAdminAccess = true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\nFound:").append(problems.size()).append(" problems:\n");
            for (String problem : problems) {
                sb.append(problem);
                sb.append("\n");
            }

            return sb.toString();
        }

    }

    private void buildProblemsForMissingAdminAcessProfile(TestContext context, Map<Class<?>, InspData> map) {
        for (Class<?> clazz : map.keySet()) {
            InspData data = map.get(clazz);
            if (data.isAdminAPIDetectedWithoutAdminAccessProfile()) {
                context.problems.append("\nAdmin API contains parts not tagged with ADMIN_ACCESS profile: " + data.toString());
                context.amountOfMissingAdminAPIProfileTags++;
            } else {
                context.problems.append("\nAdmin API other problems detected: " + data.toString());
            }
        }
    }

    private Map<Class<?>, InspData> buildMapOfStepsNeedingRestDoc(Reflections reflections) throws IllegalAccessException, InvocationTargetException {
        Map<Class<?>, InspData> map = new LinkedHashMap<>();
        Set<Method> methodsHavingRequestMapping = reflections.getMethodsAnnotatedWith(RequestMapping.class);
        for (Method methodOfRequestMapping : methodsHavingRequestMapping) {
            Annotation annotation = methodOfRequestMapping.getAnnotation(RequestMapping.class);
            inspectMethodAnnotatedWithRequestMApping(map, methodOfRequestMapping, annotation);
        }
        return map;
    }

    private void inspectMethodAnnotatedWithRequestMApping(Map<Class<?>, InspData> map, Method method, Annotation annotation)
            throws IllegalAccessException, InvocationTargetException {
        Class<? extends Annotation> clazz = annotation.getClass();

        for (Method methodInAnnotation : clazz.getDeclaredMethods()) {

            String name = methodInAnnotation.getName();
            if ("path".equals(name)) {
                /* path found */
                Object value = methodInAnnotation.invoke(annotation);
                String[] valuesArray = (String[]) value;
                for (String path : valuesArray) {
                    if (path.startsWith(APIConstants.API_ADMINISTRATION)) {
                        /* found - check access restricted */
                        Class<?> declaringClass = method.getDeclaringClass();
                        if (declaringClass.getName().startsWith("com.mercedesbenz.sechub.pds")) {
                            continue;
                        }
                        if (!isMethodOrClassAnnotatedWithAdminAccessProfile(method)) {
                            InspData data = map.computeIfAbsent(clazz, key -> InspData.create(key));
                            data.addMissingAdminAccess("No profile 'admin_access' defined in " + method.toString());
                        }
                        String classname = declaringClass.getSimpleName();
                        if (!classname.endsWith("AdministrationRestController")) {
                            InspData data = map.computeIfAbsent(clazz, key -> InspData.create(key));
                            data.addNameConventionProblem(
                                    "Rest controller for admin api must end with AdministrationRestController: but not like:" + classname);
                        }

                    }
                }
            }
        }
    }

    private boolean isMethodOrClassAnnotatedWithAdminAccessProfile(Method method) {
        Profile[] profilesOnMethod = method.getAnnotationsByType(Profile.class);
        boolean found = testAdminAccessProfileFound(profilesOnMethod);
        if (found) {
            return true;
        }
        Profile[] profilesOnClass = method.getDeclaringClass().getAnnotationsByType(Profile.class);
        found = testAdminAccessProfileFound(profilesOnClass);

        return found;
    }

    private boolean testAdminAccessProfileFound(Profile[] profilesOnMethod) {
        for (Profile profile : profilesOnMethod) {
            try {
                Method valueMethod = profile.getClass().getDeclaredMethod("value");
                Object value = valueMethod.invoke(profile);
                String[] profiles = (String[]) value;
                for (String strProfile : profiles) {
                    if (Profiles.ADMIN_ACCESS.equals(strProfile)) {
                        return true;
                    }
                }

            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new IllegalStateException("Wrong test implementation", e);
            }
        }
        return false;
    }

}
