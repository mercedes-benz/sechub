// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sereco.metadata.Classification;

public class CheckmarxCategoriesToClassificationConverterTest {

	private CheckmarxCategoriesToClassificationConverter converterToTest;
	
	@Before
	public void before() {
		converterToTest= new CheckmarxCategoriesToClassificationConverter();
	}

	@Test
	public void convertNullOnNullClassification() {
		assertNull(converterToTest.convert(null, null));
	}
	
	@Test
	public void convertNullReturnsClassification() {
		assertNotNull(converterToTest.convert(null,new Classification()));
	}
	
	
	@Test
	public void classification_empty_values() {
		Classification classification = converterToTest.convert(";,;,;",new Classification());
		assertEquals("",classification.getOwasp());
		assertEquals("",classification.getPci31());
		assertEquals("",classification.getPci32());
		
	}
	@Test
	public void classification_example1() {
		/* execute */
		Classification classification = converterToTest.convert("PCI DSS v3.2;PCI DSS (3.2) - 6.5.8 - Improper access control,OWASP Top 10 2013;A4-Insecure Direct Object References,OWASP Top 10 2017;A5-Broken Access Control",new Classification());
	
		assertEquals("6.5.8", classification.getPci32());
		assertEquals("A5", classification.getOwasp()); /* must be latest , so 2017 */
		
	
	}
	@Test
	public void classification_example2() {
		/* execute */
		Classification classification = converterToTest.convert("FISMA 2014;Identification And Authentication,NIST SP 800-53;AC-3 Access Enforcement (P1)",new Classification());
		
		assertEquals("Identification And Authentication", classification.getFisma());
		assertEquals("AC-3", classification.getNist()); 
		
		
	}

}
