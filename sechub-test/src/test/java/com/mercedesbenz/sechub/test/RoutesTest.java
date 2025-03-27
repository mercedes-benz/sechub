// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.user.UserAdministrationRestController;
import com.mercedesbenz.sechub.domain.schedule.SchedulerRestController;
import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSUseCaseDefinition;
import com.mercedesbenz.sechub.pds.usecase.PDSUseCaseIdentifier;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

import jakarta.annotation.security.RolesAllowed;

@SuppressWarnings("rawtypes")
public class RoutesTest {

    private static final String COM_MERCEDESBENZ_SECHUB = "com.mercedesbenz.sechub";

    private static final Logger LOG = LoggerFactory.getLogger(RoutesTest.class);

    private static Inspector inspector;

    @BeforeAll
    static void beforeAll() {

        inspector = new Inspector();
        inspector.inspect();

    }

    @ParameterizedTest(name = "#{index}:{2} {3} - {1}")
    @ArgumentsSource(TestParameterArgumentsProvider.class)
    void routes_assigned_to_right_roles(TestParameter testParameter, String simpleName, String httpCallDescription, Set<String> roles) {
        assertTestLogicCorrect(testParameter);

        if (isPDSClass(testParameter.clazz)) {
            checkPDS(testParameter);
        } else {
            checkSecHub(testParameter);
        }

    }

    @Test
    void sanitycheck_all_classes_for_rest_controllers_used_by_testparameters() {

        /* test */
        Collection<Class<?>> clazzSet = new HashSet<>();
        List<TestParameter> allTestparameters = inspector.getTestparameters();

        for (TestParameter testParameter : allTestparameters) {
            clazzSet.add(testParameter.clazz);
        }
        List<String> all1 = inspector.allClassesWithRestControllerAnnotation.stream().map((clazz) -> clazz.getName()).sorted().collect(Collectors.toList());
        List<String> all2 = clazzSet.stream().map((clazz) -> clazz.getName()).sorted().collect(Collectors.toList());

        assertEquals(asString(all1), asString(all2), "Testparameters must contain all classes with rest controller annotations!");

    }

    @Test
    void sanitycheck_usecases_for_pds_and_sechub_have_rest_controller_methods_annotations_where_necessary() {

        /* test */
        List<TestParameter> allTestparameters = inspector.getTestparameters();

        assertEverySecHubUsecaseWithRestAPIHasAtLestOneRestMethod(allTestparameters);
        assertEveryPDSUsecaseWithRestAPIHasAtLestOneRestMethod(allTestparameters);
    }

    @Test
    void spotCheck() {
        assertRestControllerChecked(SchedulerRestController.class);
        assertRestControllerChecked(UserAdministrationRestController.class);
    }

    private void assertEverySecHubUsecaseWithRestAPIHasAtLestOneRestMethod(List<TestParameter> allTestparameters) {
        for (UseCaseIdentifier sechubUsecase : UseCaseIdentifier.values()) {
            boolean foundAtLeastOneMethodForThisUseCase = false;
            for (TestParameter param : allTestparameters) {
                /* UC must be found at least one time */
                for (String foundId : param.methodData.usecaseUniqueids) {
                    foundAtLeastOneMethodForThisUseCase = sechubUsecase.uniqueId().equals(foundId);
                    if (foundAtLeastOneMethodForThisUseCase) {
                        break;
                    }
                }
                if (foundAtLeastOneMethodForThisUseCase) {
                    break;
                }
            }
            if (sechubUsecase.hasRestApi() && !foundAtLeastOneMethodForThisUseCase) {
                fail("Did not found a method for usecase: " + sechubUsecase.name() + " - " + sechubUsecase.uniqueId());
            } else if (!sechubUsecase.hasRestApi() && foundAtLeastOneMethodForThisUseCase) {
                fail("Did found a method for usecase: " + sechubUsecase.name() + " - " + sechubUsecase.uniqueId() + " (but is not marked to have REST api...)");
            }
        }
    }

    private void assertEveryPDSUsecaseWithRestAPIHasAtLestOneRestMethod(List<TestParameter> allTestparameters) {
        for (PDSUseCaseIdentifier pdsUsecase : PDSUseCaseIdentifier.values()) {

            boolean foundAtLeastOneMethodForThisUseCase = false;
            for (TestParameter param : allTestparameters) {
                /* UC must be found at least one time */
                for (String foundId : param.methodData.usecaseUniqueids) {
                    foundAtLeastOneMethodForThisUseCase = pdsUsecase.uniqueId().equals(foundId);
                    if (foundAtLeastOneMethodForThisUseCase) {
                        break;
                    }
                }
                if (foundAtLeastOneMethodForThisUseCase) {
                    break;
                }
            }
            if (pdsUsecase.hasRestApi() && !foundAtLeastOneMethodForThisUseCase) {
                fail("Did not found a method for usecase: " + pdsUsecase.name() + " - " + pdsUsecase.uniqueId());
            } else if (!pdsUsecase.hasRestApi() && foundAtLeastOneMethodForThisUseCase) {
                fail("Did found a method for usecase: " + pdsUsecase.name() + " - " + pdsUsecase.uniqueId() + " (but is not marked to have REST api...)");
            }
        }
    }

    private void assertRestControllerChecked(Class<?> clazz) {
        for (RouteData route : inspector.getResult()) {
            if (route.controller.clazz.equals(clazz)) {
                return;
            }
        }
        List<String> allClassNames = inspector.allClassesWithRestControllerAnnotation.stream().map((c) -> c.getName()).sorted().collect(Collectors.toList());

        assertEquals(clazz.getName(), asString(allClassNames), "Class :" + clazz + " was not inspected!");
    }

    private String asString(List<String> list) {
        return list.stream().collect(Collectors.joining("\n"));
    }

    private void assertTestLogicCorrect(TestParameter testParameter) {
        if (testParameter.route == null) {
            throw new IllegalStateException("Testcase locgic corrupt or real problem: No route defined for\nclazz:" + testParameter.clazz + "\nmethod:"
                    + testParameter.methodData.methodName);
        }
    }

    private void checkSecHub(TestParameter parameter) {
        String route = parameter.route;
        Class<?> clazz = parameter.clazz;
        Set<String> roles = parameter.roles;

        boolean anonymousAccessWanted = route.contains("/api/anonymous/") || route.contains("/actuator/")
                || parameter.methodData.controller.isErrorController();

        if (!anonymousAccessWanted) {
            assertMustContainAtLeastOneRole(route, clazz, roles);
        }

        if (anonymousAccessWanted) {
            assertMayNotContainAnyRole(route, clazz, roles);

        } else if (route.contains("/api/admin")) {
            assertMustHaveExpectedRoles(route, clazz, roles, RoleConstants.ROLE_SUPERADMIN);

        } else if (route.contains("/api/project") || route.contains("/api/job")) {
            assertMustHaveAtLeastOneOfExpectedRoles(route, clazz, roles, RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN);

        } else if (route.contains("/api/management")) {
            assertMustHaveAtLeastOneOfExpectedRoles(route, clazz, roles, RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER);

        } else {
            assertMustHaveAtLeastRoles(route, clazz, roles, RoleConstants.ROLE_USER);

        }
    }

    private void checkPDS(TestParameter parameter) {
        String route = parameter.route;
        Class<?> clazz = parameter.clazz;
        Set<String> roles = parameter.roles;

        boolean anonymousAccessWanted = route.contains("/api/anonymous/") || route.contains("/actuator/")
                || parameter.methodData.controller.isErrorController();
        if (!anonymousAccessWanted) {
            assertMustContainAtLeastOneRole(route, clazz, roles);
        }

        if (anonymousAccessWanted) {
            assertMayNotContainAnyRole(route, clazz, roles);

        } else if (route.contains("/api/admin")) {
            assertMustHaveExpectedRoles(route, clazz, roles, PDSRoleConstants.ROLE_SUPERADMIN);

        } else if (route.contains("/api/project") || route.contains("/api/job")) {
            assertMustHaveExpectedRoles(route, clazz, roles, PDSRoleConstants.ROLE_USER, PDSRoleConstants.ROLE_SUPERADMIN);

        } else {
            assertMustHaveAtLeastRoles(route, clazz, roles, PDSRoleConstants.ROLE_USER);
        }
    }

    private void assertMustContainAtLeastOneRole(String route, Class<?> clazz, Set<String> roles) {
        if (roles.size() > 0) {
            return;
        }
        fail(clazz.getName() + "\nwith route " + route + "\nmust contain at least one role but has none.");
    }

    private void assertMayNotContainAnyRole(String route, Class<?> clazz, Set<String> roles) {
        if (roles.size() == 0) {
            return;
        }
        fail(clazz.getName() + "\nwith route " + route + "\nshould not contain roles.\nRoles found:" + roles.toString());
    }

    private void assertMustHaveExpectedRoles(String route, Class<?> clazz, Set<String> roles, String... expectedRoles) {
        assertMustHaveAtLeastRoles(route, clazz, roles, expectedRoles);

        if (expectedRoles.length != roles.size()) {
            fail("For " + clazz.getName() + "\nthe amount of roles differs!\nExpected: " + expectedRoles.length + " but had: " + roles.size()
                    + ".\nRoles found:" + roles);
        }
    }

    private void assertMustHaveAtLeastOneOfExpectedRoles(String route, Class<?> clazz, Set<String> roles, String... expectedRoles) {
        assertMustContainAtLeastOneRole(route, clazz, roles);
        for (String expectedRole : expectedRoles) {
            if (roles.contains(expectedRole)) {
                return;
            }
        }
        fail(clazz.getName() + "\nwith route " + route + "\nshould contain one of roles: " + Arrays.asList(expectedRoles) + ".\nRoles Found:"
                + roles.toString());
    }

    private void assertMustHaveAtLeastRoles(String route, Class<?> clazz, Set<String> roles, String... expectedRoles) {
        for (String expectedRole : expectedRoles) {
            if (!roles.contains(expectedRole)) {
                fail(clazz.getName() + "\nwith route " + route + "\nshould contain role " + expectedRole + ".\nRoles Found:" + roles.toString());
            }
        }
    }

    private static boolean isPDSClass(Class<?> clazz) {
        return clazz.getName().startsWith("com.mercedesbenz.sechub.pds");
    }

    static class TestParameterArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return inspector.getTestparameters().stream().map(this::mapToArguments);
        }

        private Arguments mapToArguments(TestParameter parameter) {

            return Arguments.arguments(parameter, parameter.clazz.getSimpleName(), parameter.httpMethods + " " + parameter.route, parameter.roles);
        }

    }

    static class TestParameter implements Comparable<TestParameter> {
        MethodData methodData;
        String route;
        String httpMethods;
        Class<?> clazz;
        Set<String> roles;

        @Override
        public int compareTo(TestParameter o) {
            String r1 = route;
            String r2 = o.route;

            if (r1 == null) {
                r1 = "null";
            }
            if (r2 == null) {
                r2 = "null";
            }
            return r1.compareTo(r2);
        }

        @Override
        public String toString() {
            return "TestParameter:" + clazz.getSimpleName() + " " + methodData.toString() + "\n";
        }
    }

    static class RouteData {

        ControllerData controller = new ControllerData(this);

    }

    static class ControllerData {
        Class<?> clazz;
        String path;
        List<String> roles = new ArrayList<>();
        List<MethodData> methods = new ArrayList<>();
        RouteData route;

        ControllerData(RouteData route) {
            this.route = route;
        }

        public boolean isErrorController() {
            String clazzName = null;
            if (clazz != null) {
                clazzName = clazz.getName();
            } else {
                clazzName = "";
            }
            boolean errorController = clazzName.endsWith("ErrorController");
            return errorController;
        }

    }

    static class MethodData {

        ControllerData controller;
        String path;
        String effectivePath;
        String methodName;
        String httpMethods;
        List<String> roles = new ArrayList<>();
        List<String> effectiveRoles = new ArrayList<>();
        List<String> usecaseUniqueids;

        public MethodData(ControllerData controller) {
            this.controller = controller;
        }

        @Override
        public String toString() {
            return "#" + methodName + "(" + httpMethods + " " + path + ")";
        }
    }

    private static class Inspector {

        private List<Class> allClassesWithRestControllerAnnotation;

        List<RouteData> result = new ArrayList<>();

        private List<TestParameter> testParameters;

        private AnnotationFinder annotationFinder = new AnnotationFinder();

        public List<RouteData> getResult() {
            return result;
        }

        public void inspect() {
            allClassesWithRestControllerAnnotation = collectAllClassesForAnnotationType(RestController.class);
            for (Class clazz : allClassesWithRestControllerAnnotation) {
                inspect(clazz);
            }
        }

        public List<TestParameter> getTestparameters() {
            if (testParameters == null) {
                testParameters = createParameters();
            }
            return testParameters;
        }

        private List<TestParameter> createParameters() {
            List<TestParameter> parameters = new ArrayList<>();

            for (RouteData route : inspector.getResult()) {
                Class<?> clazz = route.controller.clazz;
                for (MethodData method : route.controller.methods) {
                    TestParameter parameter = new TestParameter();
                    parameter.clazz = clazz;
                    parameter.methodData = method;
                    parameter.httpMethods = method.httpMethods;
                    parameter.roles = new TreeSet<>(method.effectiveRoles);
                    parameter.route = method.effectivePath;

                    parameters.add(parameter);
                }
            }
            Collections.sort(parameters);
            return parameters;
        }

        private void inspect(Class clazz) {
            RouteData route = new RouteData();

            route.controller.clazz = clazz;
            route.controller.path = collectPathOrNull(clazz);
            route.controller.roles.addAll(asSafeList(collectRoles(clazz)));

            for (Method method : clazz.getDeclaredMethods()) {
                inspect(route, method);
            }

            result.add(route);
        }

        private void inspect(RouteData route, Method method) {
            RequestMappingData requestMappingData = findRequestMappingData(method);

            if (requestMappingData.isNotAdopted()) {
                /* no request mapping annotated for this class - so ignore it */
                return;
            }
            MethodData methodData = new MethodData(route.controller);

            methodData.httpMethods = resolveHttpMethodName(requestMappingData);
            methodData.usecaseUniqueids = resolveUseCaseIds(method);
            methodData.methodName = method.getName();
            methodData.roles.addAll(asSafeList(collectRoles(method)));
            methodData.path = collectPathOrNull(method);

            String controllerPath = route.controller.path;
            methodData.effectivePath = controllerPath != null ? controllerPath + methodData.path : methodData.path;

            methodData.effectiveRoles.addAll(route.controller.roles);
            methodData.effectiveRoles.addAll(methodData.roles);

            route.controller.methods.add(methodData);
        }

        private RequestMappingData findRequestMappingData(Method method) {
            return commonFindRequestMappingData(method, annotationFinder);
        }

        private RequestMappingData findRequestMappingData(Class<?> clazz) {
            return commonFindRequestMappingData(clazz, annotationFinder);
        }

        private RequestMappingData commonFindRequestMappingData(Object obj, AnnotationFinder finder) {
            RequestMappingData data = new RequestMappingData();
            data.adopt(finder.findAnnotation(obj, RequestMapping.class));
            data.adopt(finder.findAnnotation(obj, GetMapping.class));
            data.adopt(finder.findAnnotation(obj, PostMapping.class));
            data.adopt(finder.findAnnotation(obj, PutMapping.class));
            data.adopt(finder.findAnnotation(obj, DeleteMapping.class));
            data.adopt(finder.findAnnotation(obj, PatchMapping.class));
            return data;
        }

        private class AnnotationFinder {

            public <A extends Annotation> A findAnnotation(Object something, Class<A> annotationType) {
                if (something instanceof Class) {
                    return AnnotationUtils.findAnnotation((Class) something, annotationType);
                }
                if (something instanceof Method) {
                    return AnnotationUtils.findAnnotation((Method) something, annotationType);
                }
                throw new IllegalArgumentException("Not accepted:" + something);
            }
        }

        /**
         * A simple common class to handle
         * <ul>
         * <li>RequestMappingData</li>
         * <li>GetMapping</li>
         * <li>PostMapping</li>
         * <li>PutMapping</li>
         * <li>DeleteMapping</li>
         * <li>PatchMapping</li>
         * </ul>
         * in same way
         *
         * @author Albert Tregnaghi
         *
         */
        private class RequestMappingData {

            private Set<String> pathes = new TreeSet<>();
            private Set<RequestMethod> methods = new TreeSet<>();
            private boolean adopted;

            public boolean isNotAdopted() {
                return !adopted;
            }

            public void adopt(RequestMapping annotation) {
                if (annotation == null) {
                    return;
                }
                adopted = true;
                adoptPathes(annotation.value());
                adoptMethods(annotation.method());
            }

            public void adopt(PatchMapping annotation) {
                if (annotation == null) {
                    return;
                }
                adopted = true;
                adoptPathes(annotation.value());
                adoptMethod(RequestMethod.PATCH);

            }

            public void adopt(DeleteMapping annotation) {
                if (annotation == null) {
                    return;
                }
                adopted = true;
                adoptPathes(annotation.value());
                adoptMethod(RequestMethod.DELETE);

            }

            public void adopt(PutMapping annotation) {
                if (annotation == null) {
                    return;
                }
                adopted = true;
                adoptPathes(annotation.value());
                adoptMethod(RequestMethod.PUT);
            }

            public void adopt(GetMapping annotation) {
                if (annotation == null) {
                    return;
                }
                adopted = true;
                adoptPathes(annotation.value());
                adoptMethod(RequestMethod.GET);

            }

            public void adopt(PostMapping annotation) {
                if (annotation == null) {
                    return;
                }
                adopted = true;
                adoptPathes(annotation.value());
                adoptMethod(RequestMethod.POST);

            }

            private void adoptMethods(RequestMethod[] method) {
                if (method == null) {
                    return;
                }
                methods.addAll(Arrays.asList(method));
            }

            private void adoptMethod(RequestMethod method) {
                if (method == null) {
                    return;
                }
                methods.add(method);
            }

            private void adoptPathes(String... pathes) {
                if (pathes == null) {
                    return;
                }
                this.pathes.addAll(Arrays.asList(pathes));
            }

        }

        public List<String> resolveUseCaseIds(Method method) {
            List<String> list = new ArrayList<>();
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> type = annotation.annotationType();
                UseCaseDefinition[] usecaseDef = type.getAnnotationsByType(UseCaseDefinition.class);
                if (usecaseDef != null && usecaseDef.length == 1) {
                    UseCaseIdentifier identifier = usecaseDef[0].id();
                    list.add(identifier.uniqueId());
                }
                PDSUseCaseDefinition[] pdsUsecaseDef = type.getAnnotationsByType(PDSUseCaseDefinition.class);
                if (pdsUsecaseDef != null && pdsUsecaseDef.length == 1) {
                    PDSUseCaseIdentifier identifier = pdsUsecaseDef[0].id();
                    list.add(identifier.uniqueId());
                }
            }
            return list;
        }

        private String resolveHttpMethodName(RequestMappingData mapping) {
            StringBuilder sb = new StringBuilder();
            if (mapping != null) {
                for (RequestMethod requestMethod : mapping.methods) {
                    if (sb.length() != 0) {
                        sb.append(",");
                    }
                    sb.append(requestMethod.name());
                }
            }
            return sb.toString();
        }

        private String collectPathOrNull(Method method) {
            return collectPathOrNull(findRequestMappingData(method));
        }

        private String collectPathOrNull(Class<?> clazz) {
            return collectPathOrNull(findRequestMappingData(clazz));
        }

        private String collectPathOrNull(RequestMappingData requestMapping) {
            if (requestMapping == null) {
                return null;
            }
            Set<String> pathes = requestMapping.pathes;
            if (pathes == null) {
                throw new IllegalArgumentException("pathes may not be null");
            }
            int pathSize = pathes.size();
            if (pathSize == 0) {
                return null;
            }
            if (pathSize != 1) {
                throw new IllegalArgumentException("pathes length:" + pathSize);
            }
            return pathes.iterator().next();

        }

        private List<String> asSafeList(String[] strings) {
            List<String> list = new ArrayList<>();

            if (strings != null) {
                list.addAll(Arrays.asList(strings));
            }

            return list;

        }

        private String[] collectRoles(Class clazz) {
            return collectRoles(AnnotationUtils.findAnnotation(clazz, RolesAllowed.class));
        }

        private String[] collectRoles(Method method) {
            return collectRoles(AnnotationUtils.findAnnotation(method, RolesAllowed.class));
        }

        private String[] collectRoles(RolesAllowed roles) {
            if (roles != null) {
                return roles.value();
            }

            return new String[] {};
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Class> collectAllClassesForAnnotationType(Class clazz) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.addActiveProfile(Profiles.ADMIN_ACCESS);
        environment.addActiveProfile(Profiles.POSTGRES);
        environment.addActiveProfile(Profiles.PROD);
        environment.addActiveProfile(Profiles.MOCKED_PRODUCTS);

        environment.addActiveProfile(PDSProfiles.POSTGRES);
        environment.addActiveProfile(PDSProfiles.PROD);

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false, environment);
        scanner.addIncludeFilter(new AnnotationTypeFilter(clazz));

        Set<BeanDefinition> allRestControllerBeans = scanner.findCandidateComponents(COM_MERCEDESBENZ_SECHUB);

        return allRestControllerBeans.stream().map(bean -> {
            String beanClassName = bean.getBeanClassName();
            try {
                return Class.forName(beanClassName);
            } catch (Exception e) {
                LOG.trace("Was not able inspect bean", beanClassName, e);
            }
            return null;
        }).collect(Collectors.toList());
    }

}
