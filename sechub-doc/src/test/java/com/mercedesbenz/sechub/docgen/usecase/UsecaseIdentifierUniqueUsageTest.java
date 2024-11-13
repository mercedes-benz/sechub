// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.usecase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.mercedesbenz.sechub.docgen.reflections.Reflections;
import com.mercedesbenz.sechub.docgen.util.ReflectionsFactory;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class UsecaseIdentifierUniqueUsageTest {

    /**
     * This test is very important: if two annotations are using the same
     * {@link UseCaseIdentifier} as id the documentation will forget the second one!
     * So its really important that this is shown by an automated test
     */
    @Test
    public void usecases_are_using_identifiers_only_one_time_means_unique() {
        Reflections reflections = ReflectionsFactory.create();

        Map<String, String> map = new HashMap<>();
        Set<Class<?>> usesCaseAnnotations = reflections.getTypesAnnotatedWith(UseCaseDefinition.class);
        for (Class<?> clazz : usesCaseAnnotations) {
            UseCaseDefinition def = clazz.getAnnotation(UseCaseDefinition.class);
            String enumName = def.id().name();
            String annotationName = clazz.getSimpleName();

            String foundAnnotationName = map.get(enumName);
            if (foundAnnotationName != null) {
                throw new IllegalStateException("Duplicate usage of UseCaseIdentifier found!\n" + annotationName + " uses identifier enum:" + enumName + ","
                        + "\n but this id is already used by:\n" + foundAnnotationName);
            }
            map.put(enumName, annotationName);
        }
    }

    /**
     * This test is very important: if two annotations are using the same apiName,
     * the OpenAPI file will be messed up. It is really important that an automated
     * test checks for the apiName uniqueness.
     */
    @Test
    public void usecases_are_using_apinames_only_one_time_means_unique() {
        Reflections reflections = ReflectionsFactory.create();

        Map<String, String> map = new HashMap<>();
        Set<Class<?>> usesCaseAnnotations = reflections.getTypesAnnotatedWith(UseCaseDefinition.class);
        for (Class<?> clazz : usesCaseAnnotations) {
            UseCaseDefinition useCaseDefinition = clazz.getAnnotation(UseCaseDefinition.class);

            String apiName = useCaseDefinition.apiName();
            if (APIConstants.NO_API_AVAILABLE.equals(apiName)) {
                continue;
            }
            String annotationName = clazz.getSimpleName();

            String foundAnnotationName = map.get(apiName);
            if (foundAnnotationName != null) {
                throw new IllegalStateException("Duplicate usage of apiName() found!\n" + annotationName + " uses API name: " + apiName
                        + ", \n but this API name is already used by: " + foundAnnotationName);
            }
            map.put(apiName, annotationName);
        }
    }

}
