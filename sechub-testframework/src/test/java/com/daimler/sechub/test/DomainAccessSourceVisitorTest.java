// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class DomainAccessSourceVisitorTest {

	private DomainAccessSourceVisitor visitorToTest;
	private List<String> packages;
	private File sourceFile;

	@Test
	public void same_domains_no_problem() {
		/* execute */
		visitorToTest.visit(sourceFile, "com.daimler.sechub.domain.alpha",packages);
	
		/* test */
		assertTrue(visitorToTest.getProblems().isEmpty());
	
	}
	
	@Test
	public void different_domains_2_problem_when_2_imports() {
		/* execute */
		visitorToTest.visit(sourceFile, "com.daimler.sechub.domain.beta",packages);
	
		/* test */
		assertEquals(2, visitorToTest.getProblems().size());
	
	}

	@Before
	public void before() {
		packages = new ArrayList<>();
		sourceFile = new File(".");

		packages.add("com.daimler.sechub.domain.alpha.centauri");
		packages.add("com.daimler.sechub.domain.alpha.else");

		visitorToTest = new DomainAccessSourceVisitor();
	}

}
