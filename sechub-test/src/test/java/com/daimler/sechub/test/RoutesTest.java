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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.sharedkernel.RoleConstants;

import static org.junit.Assert.*;

@SuppressWarnings("rawtypes")
public class RoutesTest {
	
	private Map<Class, Map<String, ArrayList<String>>> classRoutesRolesMap = new HashMap<Class, Map<String, ArrayList<String>>>();
	
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
			
			for (Entry<String, ArrayList<String>> routesEntry : routesEntries.entrySet()) {
				String route = routesEntry.getKey();
				String[] roles = routesEntry.getValue().toArray(new String[routesEntry.getValue().size()]);
				
				if (!(route.contains("anonymous") || route.contains("actuator"))) {
					assertTrue(clazzName + " with route " + route + " should contain roles.", roles.length > 0);
				}
				
				if (route.contains("anonymous") || route.contains("actuator")) {
					assertTrue(clazzName + " with route " + route + " should not contain roles.", roles.length == 0);
				}
				
				if (route.contains("/api/admin")) {
					assertTrue(clazzName + " with route " + route + " should only contain one role, ROLE_SUPERADMIN.", roles.length == 1);
					assertTrue(clazzName + " with route " + route + " should contain ROLE_SUPERADMIN.", Arrays.asList(roles).contains(RoleConstants.ROLE_SUPERADMIN));
				}
				
				if (route.contains("/api/project") || route.contains("/api/job")) {
					assertTrue(clazzName + " with route " + route + " should contain ROLE_USER.", Arrays.asList(roles).contains(RoleConstants.ROLE_USER));
					assertTrue(clazzName + " with route " + route + " should contain ROLE_SUPERADMIN.", Arrays.asList(roles).contains(RoleConstants.ROLE_SUPERADMIN));
				}
			}	
		}
	}
	
	private List<Class> getAllClassesForAnnotationType(Class clazz) {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(clazz));
		
		Set<BeanDefinition> allRestControllerBeans = scanner.findCandidateComponents("com.daimler.sechub");
		
		return allRestControllerBeans.stream()
				.map( bean -> {
					try {
						return Class.forName(bean.getBeanClassName());
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				})
				.collect(Collectors.toList());
	}
	
	private void getClassUserRolePaths(Class clazz) {
		RequestMapping classMappings = AnnotationUtils.findAnnotation(clazz, RequestMapping.class);
		
		ArrayList<String> userRoles = new ArrayList<String>();
		List<String> resultPaths = new ArrayList<String>();
		
		String basePath = "";
		
		if (classMappings != null) {
			String[] paths = classMappings.path();
			for (String path : paths) {
				basePath = path;
				
				String[] classRoles = getRoles(clazz);
				
				if (classRoutesRolesMap.get(clazz) == null) {
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
				if (paths != null) {
					for (String path : paths) {
						
						String fullPath = basePath + path;
						
						resultPaths.add(basePath + path);
						
						String[] methodRoles = getRoles(method);
						
						if (classRoutesRolesMap.get(clazz) == null) {
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
