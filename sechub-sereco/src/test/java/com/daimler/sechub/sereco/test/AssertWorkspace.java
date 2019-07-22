// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.sereco.Workspace;
import com.daimler.sechub.sereco.metadata.Severity;
import com.daimler.sechub.sereco.metadata.Vulnerability;

public class AssertWorkspace {

	public static AssertWorkspace assertWorkspace(Workspace workspace) {
		return new AssertWorkspace(workspace);
	}

	private Workspace workspace;

	public AssertWorkspace(Workspace workspace) {
		this.workspace=workspace;
	}
	
	public WorkspaceAssertVulnerabilities hasVulnerabilitiesWith(Severity serverity) {
		return this.hasVulnerabilitiesWith(serverity,-1);
	}
	
	public WorkspaceAssertVulnerabilities hasVulnerabilitiesWith(Severity severity, int expectedAmount) {
		List<Vulnerability> list = new ArrayList<>();
		for (Vulnerability v : workspace.getVulnerabilties()) {
			if (severity.equals(v.getSeverity())){
				list.add(v);
			}
		}
		if (expectedAmount!=-1) {
			assertEquals("Did not expected amount of vulnerabilities having severity "+severity, expectedAmount, list.size());
		}
		return new WorkspaceAssertVulnerabilities(list);
	}
	
	public class WorkspaceAssertVulnerabilities extends AssertVulnerabilities{

		WorkspaceAssertVulnerabilities(List<Vulnerability> list) {
			super(list);
		}
		
		public AssertWorkspace and() {
			return AssertWorkspace.this;
		}
		
	}

}
