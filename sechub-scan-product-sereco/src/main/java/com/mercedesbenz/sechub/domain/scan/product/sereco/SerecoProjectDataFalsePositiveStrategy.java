// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import java.util.Map;
import java.util.regex.Pattern;

import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

public interface SerecoProjectDataFalsePositiveStrategy {

    /**
     * Checks if given vulnerability is identified as false positive by given
     * project data and the associated patterns from the pattern map.
     *
     * @param vulnerability
     * @param projectData
     * @param projectDataPatternMap
     * @return <code>true</code> when identified as false positive
     */
    boolean isFalsePositive(SerecoVulnerability vulnerability, FalsePositiveProjectData projectData, Map<String, Pattern> projectDataPatternMap);
}
