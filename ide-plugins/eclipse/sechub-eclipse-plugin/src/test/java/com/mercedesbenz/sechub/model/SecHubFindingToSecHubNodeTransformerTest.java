// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.Severity;

public class SecHubFindingToSecHubNodeTransformerTest {
	
	private SecHubFindingToFindingModelTransformer transformerToTest;

	@Before
	public void before() {
		transformerToTest = new SecHubFindingToFindingModelTransformer();
	}

	@Test
	public void adding_one_finding_with_two_stacktraces_results_in_node_containing_one_child_having_another_one_with_expected_data() {
		/* prepare */
		List<SecHubFinding> findings = new ArrayList<>();
		SecHubFinding finding = new SecHubFinding();
		finding.setName("myname");
		finding.setSeverity(Severity.HIGH);
		
		SecHubCodeCallStack stack1 = new SecHubCodeCallStack();
		stack1.setColumn(11);
		stack1.setLine(11);
		stack1.setLocation("location1");
		stack1.setRelevantPart("relevant1");
		stack1.setSource("source1");
		
		SecHubCodeCallStack stack2 = new SecHubCodeCallStack();
		stack2.setColumn(21);
		stack2.setLine(22);
		stack2.setLocation("location2");
		stack2.setRelevantPart("relevant2");
		stack2.setSource("source2");
		
		stack1.setCalls(stack2);
		finding.setCode(stack1);
		
		findings.add(finding);
		
		/* execute */
		FindingModel node = transformerToTest.transform(findings);
		
		/* test */
		assertNotNull(node);
		List<FindingNode> children = node.getFindings();
		assertEquals(1,children.size());
		
		// child
		FindingNode child1 = children.get(0);
		assertEquals(Integer.valueOf(11), child1.getColumn());
		assertEquals(1, child1.getCallStackStep());
		child1.getDescription().contentEquals("myname");

		// sub child
		FindingNode child21 = child1.getChildren().get(0);
		assertEquals(Integer.valueOf(21), child21.getColumn());
		assertEquals(2, child21.getCallStackStep());
		
		
	}

}
