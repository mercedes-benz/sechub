// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.util.JSONable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * This class represents the final resulting report data. If there will be another result
 * transformer in future, there must be a wrapper service established to
 * transform to this data <br>
 * <br>
 * We do not ignore properties like in {@link SecHubConfiguration} because here
 * we are the only writing instance
 *
 * @author Albert Tregnaghi
 */
@JsonInclude(Include.NON_NULL)
public class SecHubResult implements JSONable<SecHubResult> {

	public static final String PROPERTY_FINDINGS="findings";

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
	public Class<SecHubResult> getJSONTargetClass() {
		return SecHubResult.class;
	}
}
