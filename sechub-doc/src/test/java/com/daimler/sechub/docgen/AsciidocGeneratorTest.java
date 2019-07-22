// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.docgen.spring.ScheduleDescriptionGenerator;
import com.daimler.sechub.docgen.spring.SystemPropertiesDescriptionGenerator;
import com.daimler.sechub.docgen.util.ClasspathDataCollector;
import com.daimler.sechub.docgen.util.TextFileWriter;

public class AsciidocGeneratorTest {

	private AsciidocGenerator generatorToTest;

	@Before
	public void before() throws Exception {
		generatorToTest = new AsciidocGenerator();
		generatorToTest.collector = mock(ClasspathDataCollector.class);
		generatorToTest.propertiesGenerator = mock(SystemPropertiesDescriptionGenerator.class);
		generatorToTest.scheduleDescriptionGenerator=mock(ScheduleDescriptionGenerator.class);
		generatorToTest.writer = mock(TextFileWriter.class);

		when(generatorToTest.propertiesGenerator.generate(any())).thenReturn("properties-test");
		when(generatorToTest.scheduleDescriptionGenerator.generate(generatorToTest.collector)).thenReturn("schedule-test");
	}

	@Test
	public void system_property_target_file_location_and_name_as_expected() throws Exception {

		/* prepare */
		File parent = new File(System.getProperty("java.io.tmpdir"));
		/* execute */
		File target = AsciidocGenerator.createSystemProperyTargetFile(parent);

		/* test */
		assertTrue(target.getName().endsWith("gen_systemproperties.adoc"));
		assertEquals(parent, target.getParentFile());
	}
	
	@Test
	public void scheduling_target_file_location_and_name_as_expected() throws Exception {

		/* prepare */
		File parent = new File(System.getProperty("java.io.tmpdir"));
		/* execute */
		File target = AsciidocGenerator.createScheduleDescriptionTargetFile(parent);

		/* test */
		assertTrue(target.getName().endsWith("gen_scheduling.adoc"));
		assertEquals(parent, target.getParentFile());
	}

	@Test
	public void calls_properties_generator_and_saves() throws Exception {

		/* prepare */
		File targetFile = new File("outputfile");

		/* execute */
		generatorToTest.generateSystemPropertiesDescription(targetFile);

		/* test */
		verify(generatorToTest.propertiesGenerator).generate(any());
		verify(generatorToTest.writer).save(targetFile, "properties-test");

	}
	
	@Test
	public void calls_schedule_generator_and_saves() throws Exception {

		/* prepare */
		File targetFile = new File("outputfile");

		/* execute */
		generatorToTest.generateScheduleDescription(targetFile);

		/* test */
		verify(generatorToTest.scheduleDescriptionGenerator).generate(generatorToTest.collector);
		verify(generatorToTest.writer).save(targetFile, "schedule-test");

	}

}
