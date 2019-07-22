// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.sereco;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.AssertSecHubResult;
import com.daimler.sechub.domain.scan.SecHubResult;
import com.daimler.sechub.sereco.metadata.Classification;
import com.daimler.sechub.sereco.metadata.MetaData;
import com.daimler.sechub.sereco.metadata.Severity;
import com.daimler.sechub.sereco.metadata.Vulnerability;
import com.daimler.sechub.sharedkernel.util.JSONConverter;

public class SerecoReportToSecHubResultTransformerTest {

	private SerecoReportToSecHubResultTransformer transformerToTest;

	@Before
	public void before() {
		transformerToTest = new SerecoReportToSecHubResultTransformer();
	}

	@Test
	public void one_vulnerability_in_meta_results_in_one_finding() throws Exception{
		/* prepare */
		String converted = createMetaDataWithOneVulnerabilityFound();

		/* execute*/
		SecHubResult result = transformerToTest.transform(converted);

		/* test*/
		AssertSecHubResult.assertSecHubResult(result).hasFindings(1);
	}

	@Test
	public void transformation_of_id_finding_description_severity_and_name_are_done() throws Exception{
		/* prepare */
		String converted = createMetaDataWithOneVulnerabilityFound();

		/* execute*/
		SecHubResult result = transformerToTest.transform(converted);

		/* test*/
		/* @formatter:off */
		AssertSecHubResult.assertSecHubResult(result).
			hasFindingWithId(1).
				hasDescription("desc1").
				hasSeverity(com.daimler.sechub.domain.scan.Severity.MEDIUM).
				hasName("type1");
		/* @formatter:on */
	}


	private String createMetaDataWithOneVulnerabilityFound() {
		MetaData data = new MetaData();
		List<Vulnerability> vulnerabilities = data.getVulnerabilities();

		Vulnerability v1 = new Vulnerability();
		v1.setDescription("desc1");
		v1.setSeverity(Severity.MEDIUM);
		v1.setType("type1");
		v1.setUrl("url1");

		Classification cl = v1.getClassification();
		cl.setCapec("capec1");

		vulnerabilities.add(v1);

		String converted = JSONConverter.get().toJSON(data);
		return converted;
	}

}
