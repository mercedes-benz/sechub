// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.spring;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.docgen.util.ClasspathDataCollector;

/**
 * Necessary because we got an empty generated file
 * @author Albert Tregnaghi
 *
 */
public class ScheduleDescriptionGeneratorIntTest {
	private ScheduleDescriptionGenerator generatorToTest;

	@Before
	public void before() throws Exception {
		generatorToTest = new ScheduleDescriptionGenerator();
	}

	@Test
	public void ensure_schedule_description_is_not_an_empty_text() throws Exception {

		/* prepare */

		/* execute */
		String text = generatorToTest.generate(new ClasspathDataCollector());

		/* test */
		/* FIXME Albert Tregnaghi, 2018-01-11: with JDK 8 it works with JKD10 this does not work !*/
		assertFalse(text.isEmpty());

	}


}
