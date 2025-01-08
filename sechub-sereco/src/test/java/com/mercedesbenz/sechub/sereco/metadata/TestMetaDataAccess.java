// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

import java.util.List;

public class TestMetaDataAccess {

    public static void setClassification(SerecoVulnerability search, SerecoClassification classification) {
        search.classification = classification;
    }

    public static void setWebRequest(SerecoVulnerability search, SerecoWebRequest webRequest) {
        search.web.request = webRequest;
    }

    public static void setWebResponse(SerecoVulnerability search, SerecoWebResponse webResponse) {
        search.web.response = webResponse;
    }

    public static SerecoVulnerability createVulnerability(String type, SerecoSeverity severity, List<SerecoDetection> list, String description,
            SerecoClassification classification) {
        return new SerecoVulnerability(type, severity, list, description, classification);
    }

}
