// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveEntry;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveJobData;

public class FalsePositiveEntriesToJobFindingMapTransformer {

	
	public Map<Integer, FindingNodeFalsePositiveInfo> transform(List<FalsePositiveEntry> falsePositiveEntries, UUID jobUUID){
		Map<Integer, FindingNodeFalsePositiveInfo> falsePositiveFindingForJobMap = new HashMap<>();
		
		for (FalsePositiveEntry falsePositiveEntry: falsePositiveEntries) {
			FalsePositiveJobData jobData = falsePositiveEntry.getJobData();
			if (jobData==null) {
				continue;
			}
			if (! jobUUID.equals(jobData.getJobUUID())) {
				continue;
			}
			FindingNodeFalsePositiveInfo info = new FindingNodeFalsePositiveInfo();
			info.setComment(jobData.getComment());
			info.setFindingId(jobData.getFindingId());
			info.setJobUUID(jobData.getJobUUID());
			
			info.setAuthor(falsePositiveEntry.getAuthor());
			info.setCreated(falsePositiveEntry.getCreated());
			
			falsePositiveFindingForJobMap.put(jobData.getFindingId(), info);
		}
		
		return falsePositiveFindingForJobMap;
	}
	
}
