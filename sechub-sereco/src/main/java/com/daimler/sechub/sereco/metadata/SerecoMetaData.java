// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.metadata;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SerecoMetaData {

    private List<SerecoVulnerability> vulnerabilities = new ArrayList<>();
    private Set<SerecoAnnotation> annotations = new LinkedHashSet<>();

    public Set<SerecoAnnotation> getAnnotations() {
        return annotations;
    }

    public List<SerecoVulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

}
