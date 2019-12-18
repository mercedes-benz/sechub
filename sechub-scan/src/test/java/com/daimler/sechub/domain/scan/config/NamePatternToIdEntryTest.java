package com.daimler.sechub.domain.scan.config;

import static org.junit.Assert.*;

import org.junit.Test;

public class NamePatternToIdEntryTest {


	@Test
	public void from_valid_json_results_in_corresponding_object() {
		/* execute */
		NamePatternToIdEntry result = NamePatternToIdEntry.createFromJSON("{ 'namePattern' : 'abc', 'id' : '1'}");

		/* test */
		assertNotNull(result);
		assertEquals("abc",result.getNamePattern());
		assertEquals("1",result.getId());
	}

	@Test
	public void name_pattern_and_entry_id_are_resolved_as_defined_in_constructor() {
		/* prepare */
		/* execute */
		NamePatternToIdEntry entry = new NamePatternToIdEntry("pattern1","id1");

		/* test */
		assertEquals("pattern1",entry.getNamePattern());
		assertEquals("id1",entry.getId());
		assertNotNull(entry.getRegexp());
	}

	@Test
	public void name_pattern_null_entry_id1_is_accepted_but_no_regexp_pattern() {
		/* prepare */
		/* execute */
		NamePatternToIdEntry entry = new NamePatternToIdEntry(null,"id1");

		/* test */
		assertEquals(null,entry.getNamePattern());
		assertEquals("id1",entry.getId());
		assertNull(entry.getRegexp());
		assertFalse(entry.isMatching("abcd"));
		assertFalse(entry.isMatching("ab"));
		assertFalse(entry.isMatching(null));
	}

	@Test
	public void name_pattern_no_legal_regexp_entry_id1_is_accepted_but_no_regexp_pattern() {
		/* prepare */
		/* execute */
		NamePatternToIdEntry entry = new NamePatternToIdEntry("\\(0)","id1");

		/* test */
		assertEquals("\\(0)",entry.getNamePattern());
		assertEquals("id1",entry.getId());
		assertNull(entry.getRegexp());
		assertFalse(entry.isMatching("abcd"));
		assertFalse(entry.isMatching("ab"));
		assertFalse(entry.isMatching(null));
	}

	@Test
	public void name_pattern_abc_is_matching_abc_but_not_ab_or_null_or_not_abcd() {
		/* prepare */
		/* execute */
		NamePatternToIdEntry entry = new NamePatternToIdEntry("abc","id1");

		/* test */
		assertTrue(entry.isMatching("abc"));

		assertFalse(entry.isMatching("abcd"));
		assertFalse(entry.isMatching("ab"));
		assertFalse(entry.isMatching(null));

	}

	@Test
	public void name_pattern_abc_dot_star__is_matching_abc_and_abcd_but_not_ab_or_null() {
		/* prepare */
		/* execute */
		NamePatternToIdEntry entry = new NamePatternToIdEntry("abc.*","id1");

		/* test */
		assertTrue(entry.isMatching("abc"));
		assertTrue(entry.isMatching("abcd"));

		assertFalse(entry.isMatching("ab"));
		assertFalse(entry.isMatching(null));

	}

}
