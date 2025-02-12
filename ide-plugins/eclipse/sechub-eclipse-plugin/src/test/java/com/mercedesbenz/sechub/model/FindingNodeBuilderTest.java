// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FindingNodeBuilderTest {

	@Test
	public void location1_has_empty_filepath_and_as_filename_location1() {
		/* execute */
		FindingNode node = FindingNode.builder().setLocation("location1").build();
		
		/* test */
		assertEquals("",node.getFilePath());
		assertEquals("location1",node.getFileName());
	}
	
	@Test
	public void a_slash_b_slash_location1_has_a_slash_b_as_filepath_and_as_filename_location1() {
		/* execute */
		FindingNode node = FindingNode.builder().setLocation("a/b/location1").build();
		
		/* test */
		assertEquals("a/b",node.getFilePath());
		assertEquals("location1",node.getFileName());
	}
	
	@Test
	public void slash_location1_has_empty__as_filepath_and_as_filename_location1() {
		/* execute */
		FindingNode node = FindingNode.builder().setLocation("/location1").build();
		
		/* test */
		assertEquals("",node.getFilePath());
		assertEquals("location1",node.getFileName());
	}
	
	@Test
	public void slash_a_slash_b_slash_has_slash_a_slash_b_as_file_path_but_empty_filename() {
		/* execute */
		FindingNode node = FindingNode.builder().setLocation("/a/b/").build();
		
		/* test */
		assertEquals("/a/b",node.getFilePath());
		assertEquals("",node.getFileName());
	}
	
	@Test
	public void null_has_null_filepath_and_also_null_filename() {
		/* execute */
		FindingNode node = FindingNode.builder().setLocation(null).build();
		
		/* test */
		assertEquals(null,node.getFilePath());
		assertEquals(null,node.getFileName());
	}

}
