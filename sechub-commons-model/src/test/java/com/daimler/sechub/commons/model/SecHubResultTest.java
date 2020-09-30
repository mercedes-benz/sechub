// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import static com.daimler.sechub.commons.model.AssertSecHubResult.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class SecHubResultTest {

	@Test
	public void empty_sechub_result__to_json__returns_not_null() throws Exception {
		/* prepare */
		SecHubResult result = new SecHubResult();

		/* execute */
		String json = result.toJSON();
		System.out.println(json);

		/* test */
		assertNotNull(json);
	}

	@Test
	public void one_finding_sechub_result__json_contains_sample_data() throws Exception {
		/* prepare */
		SecHubResult result = new SecHubResult();
		SecHubFinding finding = new SecHubFinding();
		finding.setId(123);
		finding.hostnames.add("www.example.org");
		finding.hostnames.add("example.org");
		finding.target = "145.253.152.108";
		finding.severity = Severity.LOW;

		result.findings.add(finding);

		/* execute */
		String json = result.toJSON();

		/* test */
		assertNotNull(json);
		assertTrue(json.contains("145.253.152.108"));
		assertTrue(json.contains("www.example.org"));
		assertTrue(json.contains("123"));
		assertTrue(json.contains(Severity.LOW.name()));
	}

	@Test
	public void one_finding_sechub_result__json__reloaded_fromJson_finding_found_as_defined() throws Exception {
		/* prepare */
		SecHubResult result = new SecHubResult();
		SecHubFinding finding = new SecHubFinding();
		finding.setId(123);
		finding.hostnames.add("www.test.com");
		finding.hostnames.add("test1.com");
		finding.hostnames.add("test2.com");
		finding.target = "145.253.152.108";
		finding.severity = Severity.LOW;

		result.findings.add(finding);

		/* execute */
		String json = result.toJSON();
		SecHubResult reloaded = new SecHubResult().fromJSON(json);

		System.out.println(json);

		/* test @formatter:off */
		assertSecHubResult(reloaded).
			hasFindingWithId(123).
				hasHostNames("www.test.com","test1.com","test2.com").
				hasTarget("145.253.152.108").
				hasSeverity(Severity.LOW);
		/* @formatter:on */
	}

}
