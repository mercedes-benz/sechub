package com.daimler.sechub.docgen.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class RestDocPathFactoryTest {

	@Test
	public void create_variant_id_for_a_b_c_string_replaces_spaces_by_hyphen() {
		assertEquals("a-b-c",RestDocPathFactory.createVariantId("a b c"));
	}
	@Test
	public void create_variant_id_for__space_before_a_b_c_string_replaces_spaces_by_hyphen() {
		assertEquals("-a-b-c",RestDocPathFactory.createVariantId(" a b c"));
	}


}
