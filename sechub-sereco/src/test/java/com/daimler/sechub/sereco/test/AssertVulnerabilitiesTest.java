// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.test;

import java.util.Collections;

import org.junit.Test;

import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

public class AssertVulnerabilitiesTest {

    @Test
    public void healthCheck_a_vulnerability_can_be_found_by_one_part_only() {
        /* prepare */
        SerecoVulnerability v = new SerecoVulnerability();
        v.getClassification().setOwasp("owasp1");
        v.setSeverity(SerecoSeverity.HIGH);
        /* test */
        /* @formatter:off */
		AssertVulnerabilities.assertVulnerabilities(Collections.singletonList(v)).
			vulnerability().
				classifiedBy().
					owasp("owasp1").
				and().
				isContained();
		/* @formatter:on */
    }

    @Test
    public void healthCheck_empty_strings_but_only_owasp1_set_as_classification_is_contained() {
        /* prepare */
        SerecoVulnerability v = new SerecoVulnerability();
        v.getClassification().setOwasp("owasp1");
        /* test */
        /* @formatter:off */
		AssertVulnerabilities.assertVulnerabilities(Collections.singletonList(v)).
			vulnerability().
				classifiedBy().
					owasp("owasp1").
				and().
				isContained();
		/* @formatter:on */
    }

    @Test
    public void healthCheck_empty_strings_but_only_owasp1_set_but_owasp2_as_classification_is_NOT_contained() {
        /* prepare */
        SerecoVulnerability v = new SerecoVulnerability();
        v.getClassification().setOwasp("owasp2");
        /* test */
        /* @formatter:off */
		AssertVulnerabilities.assertVulnerabilities(Collections.singletonList(v)).
			vulnerability().
				classifiedBy().
					owasp("owasp1").
				and().
				isNotContained();
		/* @formatter:on */
    }
}
