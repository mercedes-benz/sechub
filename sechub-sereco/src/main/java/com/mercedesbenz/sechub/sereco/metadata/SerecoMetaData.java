// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SerecoMetaData {

    private List<SerecoVulnerability> vulnerabilities = new ArrayList<>();
    private Set<SerecoAnnotation> annotations = new LinkedHashSet<>();
    private List<SerecoLicenseDocument> licenseDocuments = new LinkedList<>();
    private SerecoVersionControl versionControl;

    public List<SerecoLicenseDocument> getLicenseDocuments() {
        return licenseDocuments;
    }

    public Set<SerecoAnnotation> getAnnotations() {
        return annotations;
    }

    public List<SerecoVulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVersionControl(SerecoVersionControl versionControl) {
        this.versionControl = versionControl;
    }

    public SerecoVersionControl getVersionControl() {
        return versionControl;
    }
}
