// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.spring;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.annotation.Scheduled;

import com.daimler.sechub.docgen.spring.SpringScheduleExtractor.ScheduleType;

public class SpringScheduleExtractorTest {
	private SpringScheduleExtractor extractorToTest;
	private Scheduled annotation;

	@Before
	public void before() throws Exception {
		extractorToTest = new SpringScheduleExtractor();

		/* prepare */
		annotation = mock(Scheduled.class);
		// mock to defaults as done in original
		when(annotation.fixedDelay()).thenReturn(-1L);
		when(annotation.fixedRate()).thenReturn(-1L);
		when(annotation.initialDelay()).thenReturn(-1L);
	}

	@Test
	public void scheduled_annotatin_has_no_content_results_in_not_null_but_empty_type_and_definition() {

		/* execute */
		SpringScheduleExtractor.SpringSchedule extracted = extractorToTest.extract(annotation);

		/* test */
		assertNotNull(extracted);
		assertEquals("", extracted.getScheduleDefinition());
		assertEquals(ScheduleType.UNDEFINED, extracted.getScheduleType());
	}

	@Test
	public void cron_definition_results_in_crontype_and_content() {
		/* prepare */

		when(annotation.cron()).thenReturn("crondata");

		/* execute */
		SpringScheduleExtractor.SpringSchedule extracted = extractorToTest.extract(annotation);

		/* test */
		assertNotNull(extracted);
		assertNotNull(extracted);
		assertEquals("crondata", extracted.getScheduleDefinition());
		assertEquals(ScheduleType.CRON, extracted.getScheduleType());
	}

	@Test
	public void fixed_delay_1234_definition_results_in_fixed_delay_and_1234_milliseconds() {
		/* prepare */

		when(annotation.fixedDelay()).thenReturn(1234L);

		/* execute */
		SpringScheduleExtractor.SpringSchedule extracted = extractorToTest.extract(annotation);

		/* test */
		assertNotNull(extracted);
		assertNotNull(extracted);
		assertEquals("fixed delay:1234", extracted.getScheduleDefinition());
		assertEquals(ScheduleType.FIXED, extracted.getScheduleType());
	}

	@Test
	public void fixed_delay_string_1234_abc_definition_results_in_fixed_delay_and_1234_abc() {
		/* prepare */

		when(annotation.fixedDelayString()).thenReturn("1234 abc");

		/* execute */
		SpringScheduleExtractor.SpringSchedule extracted = extractorToTest.extract(annotation);

		/* test */
		assertNotNull(extracted);
		assertNotNull(extracted);
		assertEquals("fixed delay:1234 abc", extracted.getScheduleDefinition());
		assertEquals(ScheduleType.FIXED, extracted.getScheduleType());
	}

	@Test
	public void fixed_rate_1234_definition_results_in_fixed_delay_and_1234_milliseconds() {
		/* prepare */

		when(annotation.fixedRate()).thenReturn(1234L);

		/* execute */
		SpringScheduleExtractor.SpringSchedule extracted = extractorToTest.extract(annotation);

		/* test */
		assertNotNull(extracted);
		assertNotNull(extracted);
		assertEquals("fixed rate:1234", extracted.getScheduleDefinition());
		assertEquals(ScheduleType.FIXED, extracted.getScheduleType());
	}

	@Test
	public void fixed_rate_string_1234_abc_definition_results_in_fixed_delay_and_1234_abc() {
		/* prepare */

		when(annotation.fixedRateString()).thenReturn("1234 abc");

		/* execute */
		SpringScheduleExtractor.SpringSchedule extracted = extractorToTest.extract(annotation);

		/* test */
		assertNotNull(extracted);
		assertNotNull(extracted);
		assertEquals("fixed rate:1234 abc", extracted.getScheduleDefinition());
		assertEquals(ScheduleType.FIXED, extracted.getScheduleType());
	}

	@Test
	public void initial_delay_12_and_fixed_rate_1234_definition_results_in_fixed_delay_and_1234_milliseconds() {
		/* prepare */

		when(annotation.initialDelay()).thenReturn(12L);
		when(annotation.fixedRate()).thenReturn(1234L);

		/* execute */
		SpringScheduleExtractor.SpringSchedule extracted = extractorToTest.extract(annotation);

		/* test */
		assertNotNull(extracted);
		assertNotNull(extracted);
		assertEquals("initial delay:12 fixed rate:1234", extracted.getScheduleDefinition());
		assertEquals(ScheduleType.FIXED, extracted.getScheduleType());
	}

	@Test
	public void initial_delay_string_12_xy_fixed_rate_string_1234_abc_definition_results_in_fixed_delay_and_1234_abc() {
		/* prepare */

		when(annotation.fixedRateString()).thenReturn("1234 abc");
		when(annotation.initialDelayString()).thenReturn("12 xy");

		/* execute */
		SpringScheduleExtractor.SpringSchedule extracted = extractorToTest.extract(annotation);

		/* test */
		assertNotNull(extracted);
		assertNotNull(extracted);
		assertEquals("initial delay:12 xy fixed rate:1234 abc", extracted.getScheduleDefinition());
		assertEquals(ScheduleType.FIXED, extracted.getScheduleType());
	}

	@Test
	public void initial_delay_string_12_xy_fixed_delay_string_1234_abc_definition_results_in_fixed_delay_and_1234_abc() {
		/* prepare */

		when(annotation.fixedDelayString()).thenReturn("1234 abc");
		when(annotation.initialDelayString()).thenReturn("12 xy");

		/* execute */
		SpringScheduleExtractor.SpringSchedule extracted = extractorToTest.extract(annotation);

		/* test */
		assertNotNull(extracted);
		assertNotNull(extracted);
		assertEquals("initial delay:12 xy fixed delay:1234 abc", extracted.getScheduleDefinition());
		assertEquals(ScheduleType.FIXED, extracted.getScheduleType());
	}
}
