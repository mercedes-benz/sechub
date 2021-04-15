// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.adapter.mock.AbstractMockedAdapter;
import com.daimler.sechub.sharedkernel.RoleConstants;

import static org.junit.Assert.*;

@SuppressWarnings("rawtypes")
public class RoutesTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMockedAdapter.class);

    private Map<Class, Map<String, ArrayList<String>>> classRoutesRolesMap = new HashMap<>();

    @Before
    public void before() {
        List<Class> allClasses = getAllClassesForAnnotationType(RestController.class);

        for (Class clazz : allClasses) {
            getClassUserRolePaths(clazz);
        }
    }

    @Test
    public void routes_assigned_to_right_roles() {

        for (Entry<Class, Map<String, ArrayList<String>>> entry : classRoutesRolesMap.entrySet()) {

            String clazzName = entry.getKey().getName();
            Map<String, ArrayList<String>> routesEntries = entry.getValue();

            for (String route : routesEntries.keySet()) {

                ArrayList<String> roles = routesEntries.get(route);

                if (!(route.contains("/api/anonymous/") || route.contains("/actuator/"))) {
                    assertTrue(clazzName + " with route " + route + " should contain roles.", roles.size() > 0);
                }

                if (route.contains("/api/anonymous/") || route.contains("/actuator/")) {
                    assertTrue(clazzName + " with route " + route + " should not contain roles.", roles.size() == 0);
                } else if (route.contains("/api/admin")) {
                    assertTrue(clazzName + " with route " + route + " should only contain one role, ROLE_SUPERADMIN.", roles.size() == 1);
                    assertTrue(clazzName + " with route " + route + " should contain ROLE_SUPERADMIN.", roles.contains(RoleConstants.ROLE_SUPERADMIN));
                } else if (route.contains("/api/project") || route.contains("/api/job")) {
                    assertTrue(clazzName + " with route " + route + " should contain ROLE_USER.", roles.contains(RoleConstants.ROLE_USER));
                    assertTrue(clazzName + " with route " + route + " should contain ROLE_SUPERADMIN.", roles.contains(RoleConstants.ROLE_SUPERADMIN));
                } else {
                    assertTrue(clazzName + " with route " + route + " should at least contain ROLE_USER.", roles.contains(RoleConstants.ROLE_USER));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Class> getAllClassesForAnnotationType(Class clazz) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(clazz));

        Set<BeanDefinition> allRestControllerBeans = scanner.findCandidateComponents("com.daimler.sechub");

        return allRestControllerBeans.stream().map(bean -> {
            try {
                return Class.forName(bean.getBeanClassName());
            } catch (Exception e) {
                LOG.trace(e.getMessage());
            }
            return null;
        }).collect(Collectors.toList());
    }

    private void getClassUserRolePaths(Class clazz) {
        RequestMapping classMappings = AnnotationUtils.findAnnotation(clazz, RequestMapping.class);

        List<String> userRoles = new ArrayList<String>();
        List<String> resultPaths = new ArrayList<String>();

        String basePath = "";

        if (classMappings != null) {
            String[] paths = classMappings.path();
            for (String path : paths) {
                basePath = path;

                String[] classRoles = getRoles(clazz);
                
                Map<String, ArrayList<String>> foundClazz = classRoutesRolesMap.get(clazz);
                
                if (foundClazz == null) {
                    classRoutesRolesMap.put(clazz, new HashMap<String, ArrayList<String>>());
                }
                if (classRoutesRolesMap.get(clazz).get(path) == null) {
                    classRoutesRolesMap.get(clazz).put(path, new ArrayList<String>());
                }

                if (classRoles != null) {
                    userRoles.addAll(Arrays.asList(classRoles));
                }
                classRoutesRolesMap.get(clazz).get(path).addAll(userRoles);
            }
        }

        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            RequestMapping methodMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
            if (methodMapping != null) {
                String[] paths = methodMapping.path();
                if (paths == null) {
                    continue;
                }

                for (String path : paths) {

                    String fullPath = basePath + path;

                    resultPaths.add(basePath + path);

                    String[] methodRoles = getRoles(method);

                    Map<String, ArrayList<String>> foundClazz = classRoutesRolesMap.get(clazz);
                    
                    if (foundClazz == null) {
                        classRoutesRolesMap.put(clazz, new HashMap<String, ArrayList<String>>());
                    }
                    if (classRoutesRolesMap.get(clazz).get(fullPath) == null) {
                        classRoutesRolesMap.get(clazz).put(fullPath, new ArrayList<String>());
                    }

                    if (methodRoles != null) {
                        userRoles.addAll(Arrays.asList(methodRoles));
                    }
                    classRoutesRolesMap.get(clazz).get(fullPath).addAll(userRoles);
                }
            }
        }
    }

    private String[] getRoles(Class clazz) {

        RolesAllowed roles = AnnotationUtils.findAnnotation(clazz, RolesAllowed.class);

        if (roles != null) {
            return roles.value();
        }

        return null;
    }

    private String[] getRoles(Method method) {

        RolesAllowed roles = AnnotationUtils.findAnnotation(method, RolesAllowed.class);

        if (roles != null) {
            return roles.value();
        }

        return null;
    }
}
