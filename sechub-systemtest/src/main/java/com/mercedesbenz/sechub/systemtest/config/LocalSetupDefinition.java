// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.List;

public class LocalSetupDefinition extends AbstractDefinition {

    private LocalSecHubDefinition secHub = new LocalSecHubDefinition();

    private List<PDSSolutionDefinition> pdsSolutions = new ArrayList<>();

    public List<PDSSolutionDefinition> getPdsSolutions() {
        return pdsSolutions;
    }

    public void setPdsSolutions(List<PDSSolutionDefinition> solutions) {
        this.pdsSolutions = solutions;
    }

    public LocalSecHubDefinition getSecHub() {
        return secHub;
    }

}
