// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * This class represents the final resulting report data. If there will be another result
 * transformer in future, there must be a wrapper service established to
 * transform to this data <br>
 * <br>
 * We do not ignore properties like in SecHubConfiguration because here
 * we are the only writing instance
 *
 * @author Albert Tregnaghi
 */
@JsonInclude(Include.NON_NULL)
public class SecHubResult implements JSONable<SecHubResult> {
    
    private static final SecHubResult IMPORTER = new SecHubResult();

	public static final String PROPERTY_FINDINGS="findings";

	long count;
	
	List<SecHubFinding> findings = new ArrayList<>();
	
	List<SecHubFinding> falsePositives;
	
	public void setCount(long count) {
		this.count = count;
	}

	public long getCount() {
		return count;
	}

	public List<SecHubFinding> getFindings() {
		return findings;
	}
	
	public List<SecHubFinding> getFalsePositives() {
        return falsePositives;
    }

	public void setFalsePositives(List<SecHubFinding> falsePositives) {
        this.falsePositives = falsePositives;
    }
	
	@Override
	public Class<SecHubResult> getJSONTargetClass() {
		return SecHubResult.class;
	}
	
	public static final SecHubResult fromJSONString(String json) {
	    return IMPORTER.fromJSON(json);
	}
}
