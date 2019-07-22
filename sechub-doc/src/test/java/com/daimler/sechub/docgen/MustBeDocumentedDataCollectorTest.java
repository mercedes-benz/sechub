// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

public class MustBeDocumentedDataCollectorTest {

	private MustBeDocumentedDataCollector collectorToTest;
	private Reflections reflections;

	@Before
	public void before() throws Exception {
		reflections = mock(Reflections.class);
		collectorToTest = new MustBeDocumentedDataCollector(reflections);
	}

	@Test
	public void collect_does_fetch_mustbedocumented__on_method_field_and_type_by_reflections() throws Exception {

		/* execute */
		collectorToTest.collect();
		
		/* test */
		verify(reflections).getMethodsAnnotatedWith(MustBeDocumented.class);
		verify(reflections).getFieldsAnnotatedWith(MustBeDocumented.class);
		verify(reflections).getTypesAnnotatedWith(MustBeDocumented.class);
	}

}
