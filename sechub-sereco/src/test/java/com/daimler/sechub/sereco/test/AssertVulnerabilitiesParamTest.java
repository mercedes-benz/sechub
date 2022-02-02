// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.test;

import static com.daimler.sechub.sereco.test.VulnerabilityTestDataKey.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.daimler.sechub.sereco.metadata.SerecoClassification;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.daimler.sechub.sereco.test.AssertVulnerabilities.VulnerabilityFinder;

@RunWith(Parameterized.class)
public class AssertVulnerabilitiesParamTest {

	public static VulnerabilityMutableTestData testData() {
		return new VulnerabilityMutableTestData();
	}
	
	
	@Parameters(name="parmeter test {index}:{0}")
    public static Collection<Object[]> data() {
    	List<Object[]> list = new ArrayList<>();
    	list.add(new Object[]{testData()});
    	for (VulnerabilityTestDataKey key: values()) {
    		list.add(new Object[]{testData().touch(key)});
    	}
    	return list;
    }
    
    @Parameter
    public VulnerabilityMutableTestData data;
    
	@Test
	public void healthCheck_full_setup_is_found() {
		/* prepare - vulnerability data */
		SerecoVulnerability v = new SerecoVulnerability();
		v.setDescription(data.get(DESCRIPTION));
		v.setSeverity(data.getSeverity());
		v.setType(data.get(TYPE));
		SerecoClassification classification = v.getClassification();
		classification.setOwasp(data.get(OWASP));
		classification.setCapec(data.get(CAPEC));
		classification.setCwe(data.get(CWE));
		classification.setOwaspProactiveControls(data.get(OWASPPROACTIVE));
		classification.setHipaa(data.get(HIPAA));
		classification.setPci31(data.get(PCI31));
		classification.setPci32(data.get(PCI32));
		
		/* test - former build data is found (or not) by assert framework */
		/* @formatter:off */
		VulnerabilityFinder finderSays = AssertVulnerabilities.assertVulnerabilities(Collections.singletonList(v)).
			vulnerability().
				withSeverity(data.getSeverity()).
				withType(data.get(TYPE)).
				isExactDefinedWebVulnerability().
				    withTarget(data.get(URL)).
				and().
				withDescriptionContaining(data.getShrinked(DESCRIPTION)).
				classifiedBy().
					owasp(data.get(OWASP)).
					cwe(data.get(CWE)).
					capec(data.get(CAPEC)).
					owaspProactiveControls(data.get(OWASPPROACTIVE)).
					hipaa(data.get(HIPAA)).
					pci31(data.get(PCI31)).
					pci32(data.get(PCI32)).
				and();
				
		if (data.hasTouchedFields()) {
			finderSays.isNotContained(); /* when changed this must not be found!*/
		}else {
			finderSays.isContained();
		}
		/* @formatter:on */
	}

}
