// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.sereco.Workspace;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

public class AssertWorkspace {

    public static AssertWorkspace assertWorkspace(Workspace workspace) {
        return new AssertWorkspace(workspace);
    }

    private Workspace workspace;

    public AssertWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public WorkspaceAssertVulnerabilities hasVulnerabilitiesWith(SerecoSeverity serverity) {
        return this.hasVulnerabilitiesWith(serverity, -1);
    }

    public WorkspaceAssertVulnerabilities hasVulnerabilitiesWith(SerecoSeverity severity, int expectedAmount) {
        List<SerecoVulnerability> list = new ArrayList<>();
        for (SerecoVulnerability v : workspace.getVulnerabilties()) {
            if (severity.equals(v.getSeverity())) {
                list.add(v);
            }
        }
        if (expectedAmount != -1) {
            assertEquals("Did not expected amount of vulnerabilities having severity " + severity, expectedAmount, list.size());
        }
        return new WorkspaceAssertVulnerabilities(list);
    }

    public class WorkspaceAssertVulnerabilities extends AssertVulnerabilities {

        WorkspaceAssertVulnerabilities(List<SerecoVulnerability> list) {
            super(list);
        }

        public AssertWorkspace and() {
            return AssertWorkspace.this;
        }

    }

}
