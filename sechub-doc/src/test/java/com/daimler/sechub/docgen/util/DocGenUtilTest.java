// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.AnnotatedElement;

import org.junit.Test;
import org.springframework.scheduling.annotation.Scheduled;

import com.daimler.sechub.docgen.DocAnnotationData;
import com.daimler.sechub.docgen.spring.SpringValueFilter;
import com.daimler.sechub.sharedkernel.MustBeDocumented;

public class DocGenUtilTest {

	@Test
	public void spring_scheduled__is_collected_by_build_method_and_set_to_data() throws Exception {
		
		/* prepare */
		AnnotatedElement element = mock(AnnotatedElement.class);
		MustBeDocumented info = mock(MustBeDocumented.class);
		Scheduled scheduled = mock(Scheduled.class);
		when(scheduled.cron()).thenReturn("crondata");
		when(element.getDeclaredAnnotation(Scheduled.class)).thenReturn(scheduled);
		
		/* execute */
		DocAnnotationData data = DocGeneratorUtil.buildDataForMustBeDocumented(info, element);
		
		/* test */
		assertEquals(scheduled,data.springScheduled);
	}
	
	@Test
	public void toCamelOne_returns_first_part_of_simple_classname_until_second_upper_case() throws Exception {
		assertEquals("Doc", DocGeneratorUtil.toCamelOne(DocGenUtilTest.class));
		assertEquals("Classpath", DocGeneratorUtil.toCamelOne(ClasspathDataCollector.class));
		assertEquals("Spring", DocGeneratorUtil.toCamelOne(SpringValueFilter.class));
		assertEquals("String", DocGeneratorUtil.toCamelOne(String.class));
		assertEquals("Class", DocGeneratorUtil.toCamelOne(Class.class));
		
	}


}
