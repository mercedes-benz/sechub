//SPDX-License-Identifier: MIT
package com.daimler.sechub.client.java.report;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

// Modified copy of: com/daimler/sechub/domain/scan/SecHubResult.java
@JsonInclude(Include.NON_NULL)
public class SecHubResult {

    public static final String PROPERTY_FINDINGS = "findings";

    long count;
    List<SecHubFinding> findings = new ArrayList<>();

    public void setCount(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public List<SecHubFinding> getFindings() {
        return findings;
    }

    @Override
    public String toString() {
        return "SecHubResult [count=" + count + ", findings=" + findings + "]";
    }
}
