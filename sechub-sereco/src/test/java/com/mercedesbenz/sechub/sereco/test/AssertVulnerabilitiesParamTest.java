// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.test;

import static com.mercedesbenz.sechub.sereco.test.TestVulnerabilityDataKey.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.mercedesbenz.sechub.sereco.metadata.SerecoClassification;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWeb;
import com.mercedesbenz.sechub.sereco.test.AssertVulnerabilities.VulnerabilityFinder;

@RunWith(Parameterized.class)
public class AssertVulnerabilitiesParamTest {

    @Parameter
    public TestVulnerabilityMutableData currentTestData;

    @Test
    public void healthCheck_full_setup_is_found() {
        /* prepare - vulnerability data */
        SerecoVulnerability testVulnerability = createVulnerabilityWithCurrentTestData();

        /* test - former build data is found (or not) by assert framework */
        // hint: when currentTestData.touch(field) was called in junit preparation phase
        // this field will change and assert mechanism must find it...
        /* @formatter:off */
		VulnerabilityFinder finderSays = AssertVulnerabilities.assertVulnerabilities(Collections.singletonList(testVulnerability)).
			vulnerability().
				withSeverity(currentTestData.getSeverity()).
				withType(currentTestData.get(TYPE)).
				isExactDefinedWebVulnerability().
				    withTarget(currentTestData.get(URL)).
				and().
				withDescriptionContaining(currentTestData.get(DESCRIPTION)).
				classifiedBy().
					owasp(currentTestData.get(OWASP)).
					cwe(currentTestData.getInt(CWE)).
					capec(currentTestData.get(CAPEC)).
					owaspProactiveControls(currentTestData.get(OWASPPROACTIVE)).
					hipaa(currentTestData.get(HIPAA)).
					pci31(currentTestData.get(PCI31)).
					pci32(currentTestData.get(PCI32)).
				and();

		if (currentTestData.hasTouchedFields()) {
			finderSays.isNotContained(); /* when changed this must not be found!*/
		}else {
		    // no fields touched, so vulnerability must be found!
			finderSays.isContained();
		}
		/* @formatter:on */
    }

    private SerecoVulnerability createVulnerabilityWithCurrentTestData() {
        SerecoVulnerability testVulnerability = new SerecoVulnerability();
        testVulnerability.setDescription(currentTestData.get(DESCRIPTION));
        testVulnerability.setSeverity(currentTestData.getSeverity());
        testVulnerability.setType(currentTestData.get(TYPE));

        SerecoWeb web = new SerecoWeb();
        web.getRequest().setTarget(currentTestData.get(URL));
        testVulnerability.setWeb(web);

        SerecoClassification classification = testVulnerability.getClassification();
        classification.setOwasp(currentTestData.get(OWASP));
        classification.setCapec(currentTestData.get(CAPEC));
        classification.setCwe("" + currentTestData.getInt(CWE));
        classification.setOwaspProactiveControls(currentTestData.get(OWASPPROACTIVE));
        classification.setHipaa(currentTestData.get(HIPAA));
        classification.setPci31(currentTestData.get(PCI31));
        classification.setPci32(currentTestData.get(PCI32));
        return testVulnerability;
    }

    public static TestVulnerabilityMutableData createTestDataElement() {
        return new TestVulnerabilityMutableData();
    }

    @Parameters(name = "parameter test {index}:{0}")
    public static Collection<Object[]> createDataForParameterizedTests() {
        List<Object[]> result = new ArrayList<>();

        // first entry has no touched elements
        result.add(new Object[] { createTestDataElement() });

        // for each key add the test data again, but touch it for the key...
        for (TestVulnerabilityDataKey key : values()) {
            result.add(new Object[] { createTestDataElement().touch(key) });
        }
        return result;
    }
}
