// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.usecase;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;

import com.daimler.sechub.docgen.reflections.Reflections;
import com.daimler.sechub.docgen.util.ReflectionsFactory;
import com.daimler.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class UsecaseIdentifierUniqueUsageTest {

	/**
	 * This test is very important: if two annotations are using the same
	 * {@link UseCaseIdentifier} as id the documentation will forget the second one!
	 * So its really important that this is shown by an automated test
	 */
	@Test
	public void usecases_are_using_identifiers_only_one_time_means_unique() {
		Reflections reflections = ReflectionsFactory.create();
		
		Map<String, String> map = new TreeMap<>();
		Set<Class<?>> usesCaseAnnotations = reflections.getTypesAnnotatedWith(UseCaseDefinition.class);
		for (Class<?> clazz : usesCaseAnnotations) {
			UseCaseDefinition def = clazz.getAnnotation(UseCaseDefinition.class);
			String enumName = def.id().name();
			String annotationName = clazz.getSimpleName();

			String foundAnnotationName = map.get(enumName);
			if (foundAnnotationName!=null) {
				throw new IllegalStateException("Duplicate usage of UseCaseIdentifier found!\n"
						+ annotationName+" uses identifier enum:"+enumName+","
								+ "\n but this id is already used by:\n"+foundAnnotationName);
			}
			map.put(enumName, annotationName);
		}
	}

}
